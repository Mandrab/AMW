implements[ "geo_localization" ].

/////////////////////////////

+!zone(X, Y) <- .random(X, 9); .random(Y, 9).                           // in this demo simply return a random zone

-!zone(X, Y) <- .println("Failed to determine position").