implements[ "move" ].

/////////////////////////////

+!move_by(X, Y)
    <-  .printf("Moving to %.2f %.2f", X, Y);
        //.wait(50);
        .println("Moved").

-!mode_by(X, Y) <- .println("Unable to move").