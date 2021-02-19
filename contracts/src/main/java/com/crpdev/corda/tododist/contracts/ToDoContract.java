package com.crpdev.corda.tododist.contracts;

import com.crpdev.corda.tododist.states.ToDoState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class ToDoContract implements Contract {

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        System.out.println("ToDoContract's verify method has been called!");


        List<CommandWithParties<CommandData>> commands = tx.getCommands();
        CommandData command = commands.get(0).getValue();

        ToDoState toDoOutput = (ToDoState) tx.getOutputStates().get(0);
        if (command instanceof Command.CreateToDoCommand){
            // Corda DSL
            requireThat(r -> {
                r.using("Task Description must not be blank!", !toDoOutput.getTaskDescription().trim().equals(""));
                r.using("Task Description must be <= 25 characters!", toDoOutput.getTaskDescription().length() < 25);
                return null;
            });
        } else if (command instanceof Command.AssignToDoCommand){
            ToDoState toDoInput = (ToDoState) tx.getInputStates().get(0);
            // Corda DSL
            requireThat(r -> {
                r.using("Task already assigned to Party", !toDoInput.getAssignedTo().equals(toDoOutput.getAssignedTo()));
                return null;
            });
        }


        if (toDoOutput.getTaskDescription().trim().equals("")) throw new IllegalArgumentException("Task Description must not be blank!");
        if (toDoOutput.getTaskDescription().length() > 25) throw new IllegalArgumentException("Task Description must be <= 25 characters!");


    }
}
