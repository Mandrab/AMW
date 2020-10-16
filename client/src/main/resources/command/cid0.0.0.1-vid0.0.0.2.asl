requirements: [ "movement", "tag_localization" ]

+!main <- !zone(X, Y)[source(tag_localization)];                    // get actual zone
        !isZoneA(X, Y).                                             // check if its zone A

+!isZoneA(1, 1) <- .println("Moved to zone A").                     // area reached

+!isZoneA(X, Y) <- .random(X); .random(Y);                          // random movement (remember its only a demo)
        !move_by(X, Y);                                             // move by random
        !zone(NewX, NewY);                                          // check reached zone
        !isZoneA(NewX, NewY).                                       // check if it's zone A
