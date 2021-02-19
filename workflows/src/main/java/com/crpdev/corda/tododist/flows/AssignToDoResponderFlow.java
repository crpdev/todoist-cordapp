package com.crpdev.corda.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

@InitiatedBy(AssignToDoInitiatorFlow.class)
public class AssignToDoResponderFlow extends FlowLogic<SignedTransaction> {

    private final FlowSession counterPartySession;

    public AssignToDoResponderFlow(FlowSession counterPartySession){
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

        return subFlow(new ReceiveFinalityFlow(counterPartySession, stx.getId()));
    }
}
