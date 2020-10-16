// find a feasible version

+!get_feasible([ Head | [] ], Script)
	:   Head = variant(v_id(ID), requirements[ [] | Requirements ], script(Script))
	&   implements[ source(self) | Labels ]
	<-  !contained(Requirements, Labels).

+!get_feasible([ Head | Tail ], Feasible)
	<-  !get_feasible([ Head ], Feasible).

-!get_feasible([ Head | Tail ], Feasible)
	<-  !get_feasible([ Tail ], Feasible).

// search left-list is contained in right-list

+!contained(H, _) : .term2string(H, X) & .empty(X).
+!contained([], L).
+!contained([H | T], L) <- !contained(H, L); !contained(T, L).
+!contained(H, [H | _]).
+!contained(H, [_ | T]) <- !contained(H, T).