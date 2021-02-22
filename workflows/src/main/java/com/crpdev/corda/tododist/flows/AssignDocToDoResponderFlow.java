package com.crpdev.corda.tododist.flows;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;


@InitiatedBy(AttachDocToDoInitiatorFlow.class)
public class AssignDocToDoResponderFlow extends FlowLogic<SignedTransaction> {

    private final FlowSession counterPartySession;

    public AssignDocToDoResponderFlow(FlowSession counterPartySession){
        this.counterPartySession = counterPartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        System.out.println("*** Received Task Assignment ***");
        final SignTransactionFlow signTransactionFlow = new SignTransactionFlow(counterPartySession) {

            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                System.out.println("Checked!");
            }
        };

        SignedTransaction stx = subFlow(signTransactionFlow);
        System.out.println("Task Assignment with attachment Accepted!");
        return subFlow(new ReceiveFinalityFlow(counterPartySession, stx.getId()));
    }
}
