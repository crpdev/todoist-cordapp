package com.crpdev.corda.tododist.states.todo;

import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import org.hibernate.annotations.Type;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.UUID;

public class ToDoSchemaV1 extends MappedSchema {

    public ToDoSchemaV1(){
        super(ToDoSchema.class, 1, Arrays.asList(ToDoModel.class));
    }

    @Entity
    @Table(name="todo_model")
    public static class ToDoModel extends PersistentState {

        @Column(name = "task")
        private final String task;

        @Column(name = "id")
        @Type(type = "uuid-char")
        private final UUID linearId;

        public ToDoModel (String task, UUID linearId){
            this.task = task;
            this.linearId = linearId;
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
