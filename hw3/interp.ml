(* Name: Derek Nguyen

   UID: 304275956

   Others With Whom I Discussed Things: Sung Hyun Yoon 904303999

   Other Resources I Consulted:
   
*)

(* EXCEPTIONS *)

(* This is a marker for places in the code that you have to fill in.
   Your completed assignment should never raise this exception. *)
exception ImplementMe of string

(* This exception is thrown when a type error occurs during evaluation
   (e.g., attempting to invoke something that's not a function).
   You should provide a useful error message.
*)
exception DynamicTypeError of string

(* This exception is thrown when pattern matching fails during evaluation. *)  
exception MatchFailure  

(* EVALUATION *)

(* See if a value matches a given pattern.  If there is a match, return
   an environment for any name bindings in the pattern.  If there is not
   a match, raise the MatchFailure exception.
*)
let rec patMatch (pat:mopat) (value:movalue) : moenv =
  match (pat, value) with
      (* an integer pattern matches an integer only when they are the same constant;
	 no variables are declared in the pattern so the returned environment is empty *)
      (IntPat(i), IntVal(j)) when i=j -> Env.empty_env()
    | (BoolPat(i), BoolVal(j)) when i=j -> Env.empty_env()
    | (WildcardPat, _) -> Env.empty_env()
    | (VarPat(s), _) -> Env.add_binding s value (Env.empty_env())
    | (TuplePat(l1), TupleVal(l2)) -> (match (l1,l2) with
                                        ([],[]) -> Env.empty_env()
                                      | ([],_) -> raise MatchFailure
                                      | (_,[]) -> raise MatchFailure
                                      | (h1::t1,h2::t2) -> (match h1 with
                                                              VarPat(s) -> Env.add_binding s h2 (patMatch (TuplePat(t1)) (TupleVal(t2)))
                                                            | TuplePat(_) -> Env.combine_envs (patMatch h1 h2) (patMatch (TuplePat(t1)) (TupleVal(t2)))
                                                            | _ -> raise MatchFailure
                                                           )
                                      )
    | (DataPat(s1,op1), DataVal(s2,op2)) when s1=s2 -> (match (op1,op2) with
                                                          (None, None) -> Env.empty_env()
                                                        | (None, _) -> raise MatchFailure
                                                        | (_, None) -> raise MatchFailure
                                                        | (Some p1, Some p2) -> (match (p1,p2) with
                                                                        (VarPat(s),_) -> Env.add_binding s p2 (Env.empty_env())
                                                                      | (TuplePat(_),TupleVal(_)) -> patMatch p1 p2
                                                                      | (DataPat(_,_),DataVal(_,_)) -> patMatch p1 p2
                                                                      | _ -> raise MatchFailure
                                                                      )
                                                       )
    | _ -> raise MatchFailure

(* Evaluate an expression in the given environment and return the
   associated value.  Raise a MatchFailure if pattern matching fails.
   Raise a DynamicTypeError if any other kind of error occurs (e.g.,
   trying to add a boolean to an integer) which prevents evaluation
   from continuing.
*)
let rec evalExpr (e:moexpr) (env:moenv) : movalue =
  match e with
      (* an integer constant evaluates to itself *)
      IntConst(i) -> IntVal(i)
    | BoolConst(i) -> BoolVal(i)
    | Var(s) -> Env.lookup s env
    | BinOp(e1,op,e2) -> (let r1 = (evalExpr e1 env) in
                          let r2 = (evalExpr e2 env) in
                          match (r1,r2) with
                            (IntVal(i),IntVal(j)) -> (match op with
                                                        Plus -> IntVal(i + j)
                                                      | Minus -> IntVal(i - j)
                                                      | Times -> IntVal(i * j)
                                                      | Eq -> BoolVal(i = j)
                                                      | Gt -> BoolVal(i > j)
                                                     )
                          | _ -> raise (DynamicTypeError "Invalid binary operation")
                          )
    | Negate(exp) -> (match (evalExpr exp env) with
                        IntVal(i) -> IntVal(-i)
                      | _ -> raise (DynamicTypeError "Invalid negation of expression")
                     )
    | If(c,t,e) -> (match (evalExpr c env) with
                      BoolVal(b) -> if b then (evalExpr t env) else (evalExpr e env)
                    | _ -> raise (DynamicTypeError "Invalid if statement")
                   )
    | Function(p, e) -> FunctionVal(None, p, e, env)
    | FunctionCall(e1,e2) -> (match (evalExpr e1 env) with 
                                FunctionVal(o,p,e,en) -> let argEnv = (patMatch p (evalExpr e2 env)) in
                                                          (match o with
                                                            None -> (evalExpr e (Env.combine_envs en argEnv))
                                                          | Some s -> (evalExpr e (Env.add_binding s (FunctionVal(o,p,e,en)) (Env.combine_envs en argEnv)))
                                                          )
                             )
    | Match(e, l) -> (match (e, l) with
                        (_,[]) -> raise MatchFailure 
                      | (_,(p,exp)::t) -> (try let en = (patMatch p (evalExpr e env)) in (evalExpr exp (Env.combine_envs env en)) with
                                            MatchFailure -> (evalExpr (Match(e,t)) env)
                                          )
                     )
    | Tuple(l) -> (match l with
                    [] -> TupleVal([])
                  | h::t -> (match ((evalExpr h env), (evalExpr (Tuple(t)) env)) with
                              (moval,TupleVal(l)) -> TupleVal(moval::l)  
                            | _ -> raise (DynamicTypeError "Invalid tuple")
                            )
                  )
    | Data(s, e) -> (match e with
                      None -> DataVal(s, None)
                    | Some exp -> DataVal(s, Some (evalExpr exp env))
                    | _ -> raise (DynamicTypeError "Invalid data")
                    )
    | _ -> raise MatchFailure


(* Evaluate a declaration in the given environment.  Evaluation
   returns the name of the variable declared (if any) by the
   declaration along with the value of the declaration's expression.
*)
let rec evalDecl (d:modecl) (env:moenv) : moresult =
  match d with
      (* a top-level expression has no name and is evaluated to a value *)
      Expr(e) -> (None, evalExpr e env)
    | Let(s, e) -> (Some s, evalExpr e env)
    | LetRec(s, e) -> (match e with
                        Function(p, exp) -> (Some s, FunctionVal(Some s, p, exp, env))
                      | _ -> raise (DynamicTypeError "Invalid function declaration")
                      )
    | _ -> raise MatchFailure

