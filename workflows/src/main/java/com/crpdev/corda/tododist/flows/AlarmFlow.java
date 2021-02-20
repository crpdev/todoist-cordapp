package com.crpdev.corda.tododist.flows;

import com.crpdev.corda.tododist.states.ToDoState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.SchedulableFlow;
import net.corda.core.node.ServiceHub;

import java.time.Instant;

@InitiatingFlow
@SchedulableFlow
public class AlarmFlow extends FlowLogic<Void> {

    private final StateRef stateRef;

    public AlarmFlow(StateRef stateRef){
        this.stateRef = stateRef;
    }

    @Override
    public Void call() throws FlowException {
        ServiceHub serviceHub = getServiceHub();
        StateAndRef<ToDoState> stateAndRef = serviceHub.toStateAndRef(stateRef);
        ToDoState toDoState = stateAndRef.getState().getData();
        serviceHub.getVaultService().addNoteToTransaction(stateAndRef.getRef().getTxhash(), "Reminder Set: " + Instant.now());
        System.out.println("Deadline approaching for the task: " + toDoState.getTaskDescription());
        return null;
    }
}
