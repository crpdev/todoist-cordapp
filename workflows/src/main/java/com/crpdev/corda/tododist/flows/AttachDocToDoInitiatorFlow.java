package com.crpdev.corda.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.crpdev.corda.tododist.contracts.Command;
import com.crpdev.corda.tododist.states.ToDoState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.AttachmentStorage;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@InitiatingFlow
@StartableByRPC
public class AttachDocToDoInitiatorFlow extends FlowLogic<Void> {

    private final String linearId;
    private final String fileUrl;
    private final String fileName;

    public AttachDocToDoInitiatorFlow(String linearId, String fileUrl, String fileName){
        this.linearId = linearId;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {

        ServiceHub serviceHub = getServiceHub();
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        AttachmentStorage attachmentService = serviceHub.getAttachments();
        BufferedInputStream in = null;
        SecureHash secureHash = null;

        try {
            InputStream inputStream = new URL(fileUrl).openStream();
            secureHash = attachmentService.importAttachment(inputStream, "ToDoDist Cordapp", fileName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        QueryCriteria q = new QueryCriteria.LinearStateQueryCriteria(null, Arrays.asList(UUID.fromString(linearId)));
        Vault.Page<ToDoState> taskStatePage = serviceHub.getVaultService().queryBy(ToDoState.class, q);
        List<StateAndRef<ToDoState>> states = taskStatePage.getStates();
        StateAndRef<ToDoState> currentToDoStateAndRef = states.get(0);
        ToDoState toDoState = currentToDoStateAndRef.getState().getData();

        List<PublicKey> signers = Arrays.asList(toDoState.getAssignedBy().getOwningKey(), toDoState.getAssignedTo().getOwningKey());

        TransactionBuilder txb = new TransactionBuilder(notary)
                .addInputState(currentToDoStateAndRef)
                .addOutputState(toDoState)
                .addCommand(new Command.AttachDocToDoCommand(), signers)
                .addAttachment(secureHash);

        SignedTransaction partiallySignedTx = getServiceHub().signInitialTransaction(txb);
//        List<AbstractParty> participants = toDoState.getParticipants();
//        participants.remove(getOurIdentity());
//
//        FlowSession counterPartySession = initiateFlow(participants.get(0));
//
//        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partiallySignedTx, Arrays.asList(counterPartySession)));
//        subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(counterPartySession)));

        if(toDoState.getAssignedBy().equals(toDoState.getAssignedTo())) {
            subFlow(new FinalityFlow(partiallySignedTx, Collections.<FlowSession>emptySet()));
        } else {
            List<AbstractParty> parties = toDoState.getParticipants();
            parties.remove(getOurIdentity());
            FlowSession assignedToSession = initiateFlow((Party)parties.get(0));
            SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partiallySignedTx,
                    Arrays.asList(assignedToSession)));
            subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(assignedToSession)));
        }

        return null;
    }
}
