package com.crpdev.corda.tododist.contracts;

import net.corda.core.contracts.CommandData;

public interface Command {

    class CreateToDoCommand implements CommandData {}
    class AssignToDoCommand implements CommandData {}
    class MarkCompleteToDoCommand implements CommandData {}

}
