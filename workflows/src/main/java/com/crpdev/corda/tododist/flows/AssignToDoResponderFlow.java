package com.crpdev.corda.tododist.flows;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.TimeUnit;


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

        try {
            Strand.sleep(70, TimeUnit.SECONDS); // Force a time window expiration - can also use FlowLogic.sleep()
            System.out.println("Simulating Sleep for 70 seconds");
//            FlowLogic.sleep(Duration.ofSeconds(70));
        } catch (SuspendExecution suspendExecution) {
            suspendExecution.printStackTrace();
            System.out.println("Caught SuspendExecution");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Caught InterruptedException");
//        } catch (Exception e) {
//            e.printStackTrace();
        }

        SignedTransaction stx = subFlow(signTransactionFlow);
        System.out.println("Task Assignment Accepted!");
        return subFlow(new ReceiveFinalityFlow(counterPartySession, stx.getId()));
    }
}
