package com.crpdev.corda.tododist.states.todo;

import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import org.hibernate.annotations.Type;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.UUID;

public class ToDoSchemaV2 extends MappedSchema {

    public ToDoSchemaV2(){
        super(ToDoSchema.class, 2, Arrays.asList(ToDoModel.class));
    }

    @Entity
    @Table(name="todo_model2")
    public static class ToDoModel extends PersistentState {

        @Column(name = "task")
        private final String task;

        @Column(name = "id")
        @Type(type = "uuid-char")
        private final UUID linearId;

        @Column(name = "assigned_to")
        private final String assignedTo;

        public ToDoModel (String task, UUID linearId, Party assignedTo){
            this.task = task;
            this.linearId = linearId;
            this.assignedTo = assignedTo.getName().getOrganisation();
        }

        public String getTask() {
            return task;
        }

        public UUID getLinearId() {
            return linearId;
        }
    }

    @Nullable
    @Override
    public String getMigrationResource() {
        return "tododist.changelog-master";
    }

}
