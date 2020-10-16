requirements: [ "movement", "geo_localization" ]

+!main <- !zone(X, Y)[source(geo_localization)];                // get position
        !move_by(X -1, Y -1)[source(movement)];                 // move by distance
        !zone(NewX, NewY)[source(geo_localization)];            // get reached zone position
        !reach(NewX, NewY).                                     // check is zone A (1, 1)

+!reach(1, 1) <- .println("Moved successfully to zone A").

+!reach(X, Y) <- .println("Error in movement, should be at 1,1 but is at %.2f %.2f" X, Y).
