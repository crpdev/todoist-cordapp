package com.crpdev.corda.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.crpdev.corda.tododist.contracts.Command;
import com.crpdev.corda.tododist.states.ToDoState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TimeWindow;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.security.PublicKey;
import java.time.Instant;
import java.util.*;

@InitiatingFlow
@StartableByRPC
public class AssignToDoInitiatorFlow extends FlowLogic<Void> {

    private final String assignedTo;
    private final String linearId;

    public AssignToDoInitiatorFlow(String assignedTo, String linearId){
        this.assignedTo = assignedTo;
        this.linearId = linearId;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        ServiceHub serviceHub =  getServiceHub();
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);

        QueryCriteria q = new QueryCriteria.LinearStateQueryCriteria(null, Collections.singletonList(UUID.fromString(linearId)));
        Vault.Page<ToDoState> taskStatePage = serviceHub.getVaultService().queryBy(ToDoState.class, q);
        List<StateAndRef<ToDoState>> states = taskStatePage.getStates();
        StateAndRef<ToDoState> currentToDoStateAndRef = states.get(0);
        ToDoState toDoState = currentToDoStateAndRef.getState().getData();

        System.out.printf("Task Linear Id: %s, Task Description: %s, Task Assignee: %s", toDoState.getLinearId(), toDoState.getTaskDescription(), toDoState.getAssignedTo().getName());

        Set<Party> parties = serviceHub.getIdentityService().partiesFromName(assignedTo, true);
        Party assignedToParty = parties.iterator().next();
        System.out.printf("New Assignee: %s", assignedToParty.getName().getOrganisation());
        ToDoState newToDoState = toDoState.assignTo(assignedToParty);

        System.out.printf("Task Linear Id: %s, Task Description: %s, Task Reporter: %s, Task Assignee: %s", newToDoState.getLinearId(), newToDoState.getTaskDescription(), newToDoState.getAssignedBy().getName().getOrganisation(), newToDoState.getAssignedTo().getName().getOrganisation());

        PublicKey myKey = getOurIdentity().getOwningKey();
        PublicKey counterPartyKey = assignedToParty.getOwningKey();
        List<PublicKey> signers = Arrays.asList(myKey, counterPartyKey);
        TransactionBuilder tx = new TransactionBuilder(notary)
                .addInputState(currentToDoStateAndRef)
                .addOutputState(newToDoState)
                .addCommand(new Command.AssignToDoCommand(), signers)
                .setTimeWindow(TimeWindow.between(Instant.now(), Instant.now().plusSeconds(10)));

        SignedTransaction partialSignedTx = getServiceHub().signInitialTransaction(tx);

        FlowSession assignedToSession = initiateFlow(assignedToParty);
//        final List<Party> requiredSigners = Arrays.asList(assignedToParty);
//        List<FlowSession> signerFlows = requiredSigners.stream()
//                .filter(it -> !it.equals(getOurIdentity()))
//                .map(this::initiateFlow)
//                .collect(Collectors.toList());
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partialSignedTx, Arrays.asList(assignedToSession)));


        subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(assignedToSession)));
        System.out.println("Linear Id: " + toDoState.getLinearId().getId());
        return null;
    }
}
