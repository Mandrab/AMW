/***********************************************************************************************************************
 Literal Utils
***********************************************************************************************************************/

+!concat( Lit, [ H | T ], Res )
	:   .literal( Lit )
	&   T = []
	<-  Res = Lit[ H ].                                             // add a list at the end of a literal

+!concat( Lit, [ H | T ], Res )
	:   .literal( Lit )
	&   not .empty( T )
	<-  !concat( Lit[ H ], T, Res ).                                // add a list at the end of a literal