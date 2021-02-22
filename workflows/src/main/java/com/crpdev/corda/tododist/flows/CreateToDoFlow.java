package com.crpdev.corda.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.crpdev.corda.tododist.contracts.Command;
import com.crpdev.corda.tododist.states.ToDoState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Collections;

@StartableByRPC
public class CreateToDoFlow extends FlowLogic<SignedTransaction> {

    private final String taskDescription;

    public CreateToDoFlow(String taskDescription){
        this.taskDescription = taskDescription;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        ServiceHub serviceHub = getServiceHub();
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        Party me = getOurIdentity();

        ToDoState ts = new ToDoState(me, me, taskDescription);

        TransactionBuilder tx = new TransactionBuilder(notary).addOutputState(ts)
                .addCommand(new Command.CreateToDoCommand(), me.getOwningKey());


        SignedTransaction stx = getServiceHub().signInitialTransaction(tx);

        subFlow(new FinalityFlow(stx, Collections.<FlowSession>emptySet()));
        System.out.println("Linear Id: " + ts.getLinearId().getId());
        return null;
    }
}
