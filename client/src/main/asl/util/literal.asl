/***********************************************************************************************************************
 Literal Utils
***********************************************************************************************************************/

+!concat(Lit,[],Lit).
+!concat(Lit,[H | []],Lit[H]) : .literal(Lit).                          // add a list at the end of a literal
+!concat(Lit,[H | T],Res) : .literal(Lit) <- !concat(Lit[H],T,Res).     // add a list at the end of a literal