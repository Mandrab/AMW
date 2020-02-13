/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!str_concat( Str1, [ Str2 | Other ], Result )
	<-  if ( not .string( Str1 ) ) { .term2string( Str1, S1 ); }                // force data as string
		else { S1 = Str1; }
		if ( not .string( Str2 ) ) { .term2string( Str2, S2 ); }                // force data as string
		else { S2 = Str2; }
		.concat( S1, S2, Res );                                                 // concat strings
		if ( not .empty( Other ) ) { !str_concat( Res, Other, Result ); }       // keep concat if tail exist
		else { Result = Res; }.

+!concat( Lit, [ H | T ], Res ) : T = [] <-  Res = Lit[ H ].                    // add a list at the end of a literal

+!concat( Lit, [ H | T ], Res ) : .list( T ) <- !concat( Lit[ H ], T, Res ).    // add a list at the end of a literal