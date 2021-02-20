package com.crpdev.corda.tododist.states;

import com.crpdev.corda.tododist.contracts.ToDoContract;
import com.crpdev.corda.tododist.states.todo.ToDoSchemaV1;
import com.crpdev.corda.tododist.states.todo.ToDoSchemaV2;
import net.corda.core.contracts.*;
import net.corda.core.flows.FlowLogicRefFactory;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@BelongsToContract(ToDoContract.class)
public class ToDoState implements ContractState, LinearState, QueryableState, SchedulableState{

    private final Party assignedBy;
    private final Party assignedTo;
    private final String taskDescription;
    private final UniqueIdentifier linearId;
    private final Instant deadlineReminder;

    public ToDoState(Party assignedBy, Party assignedTo, String taskDescription){
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
        this.linearId = new UniqueIdentifier();
        this.deadlineReminder = Instant.now().plusSeconds(30);
    }

    @ConstructorForDeserialization
    public ToDoState(Party assignedBy, Party assignedTo, String taskDescription, UniqueIdentifier linearId, Instant deadlineReminder){
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
        this.linearId = linearId;
        this.deadlineReminder = deadlineReminder;
    }

    public Party getAssignedBy() {
        return assignedBy;
    }

    public Party getAssignedTo() {
        return assignedTo;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(assignedBy, assignedTo);
    }

    public ToDoState assignTo(Party assignedTo){
        return new ToDoState(
                assignedBy, assignedTo, taskDescription, linearId, deadlineReminder
        );
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if (schema instanceof ToDoSchemaV1){
            return new ToDoSchemaV1.ToDoModel(taskDescription, linearId.getId());
        } else if (schema instanceof ToDoSchemaV2){
            return new ToDoSchemaV2.ToDoModel(taskDescription, linearId.getId(), assignedTo);
        } else {
            throw new IllegalArgumentException("No Supported Schema Found!");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return Arrays.asList(new ToDoSchemaV1(), new ToDoSchemaV2());
    }

    @Nullable
    @Override
    public ScheduledActivity nextScheduledActivity(@NotNull StateRef thisStateRef, @NotNull FlowLogicRefFactory flowLogicRefFactory) {
        System.out.println("nextScheduledActivity() invoked");
        System.out.println("StateRef TX ID is " + thisStateRef.getTxhash());
        final ScheduledActivity scheduledActivity = new
                ScheduledActivity(flowLogicRefFactory.create (
                "com.crpdev.corda.tododist.flows.AlarmFlow",thisStateRef), deadlineReminder);
        return scheduledActivity;
    }
}
