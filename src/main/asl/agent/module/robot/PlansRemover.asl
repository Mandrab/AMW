{ include("util/literal.asl") }                                     // include communication utils plans

///////////////////////////// REMOVE SCRIPT'S PLANS

@remove_plans[atomic]
+!remove_plans(IDX)
	:   .concat(l, IDX, Label) & .term2string(TLabel, Label)
	&   .plan_label(P, TLabel)
	<-  .remove_plan(TLabel, script);
		!remove_plans(IDX + 1).

-!remove_plans(IDX).