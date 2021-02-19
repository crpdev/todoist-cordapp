package com.crpdev.corda.tododist.states;

import com.crpdev.corda.tododist.contracts.ToDoContract;
import com.crpdev.corda.tododist.states.todo.ToDoSchemaV1;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(ToDoContract.class)
public class ToDoState implements ContractState, LinearState, QueryableState {

    private final Party assignedBy;
    private final Party assignedTo;
    private final String taskDescription;
    private final UniqueIdentifier linearId;

    public ToDoState(Party assignedBy, Party assignedTo, String taskDescription){
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
        this.linearId = new UniqueIdentifier();
    }

    @ConstructorForDeserialization
    public ToDoState(Party assignedBy, Party assignedTo, String taskDescription, UniqueIdentifier linearId){
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
        this.linearId = linearId;
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
                assignedBy, assignedTo, taskDescription, linearId
        );
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if (schema instanceof ToDoSchemaV1){
            return new ToDoSchemaV1.ToDoModel(taskDescription, linearId.getId());
        } else {
            throw new IllegalArgumentException("No Supported Schema Found!");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return Arrays.asList(new ToDoSchemaV1());
    }
}
