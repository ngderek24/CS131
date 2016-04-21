(*
  Derek Nguyen
  304275956

  Discussed problems with
  Sung Hyun Yoon 904303999

  Received help from
  http://caml.inria.fr/pub/docs/manual-ocaml/libref/List.html
*)

(* Problem 1: Vectors and Matrices *)

(* type aliases for vectors and matrices *)            
type vector = float list                                 
type matrix = vector list

let (vplus : vector -> vector -> vector) = fun v1 v2 ->
  List.map2 (fun x1 x2 -> x1 +. x2) v1 v2
  
let (mplus : matrix -> matrix -> matrix) = fun m1 m2 ->
  List.map2 (fun v1 v2 -> vplus v1 v2) m1 m2

let (dotprod : vector -> vector -> float) = fun v1 v2 ->
  List.fold_right (+.) (let vmult v1 v2 = List.map2 (fun x1 x2 -> x1 *. x2) v1 v2 in vmult v1 v2) 0.

let convertToCol = fun m index ->
  List.map (fun v -> List.nth v index) m

let (transpose : matrix -> matrix) = fun m ->
  match m with
    [] -> []
  | h::t -> List.mapi (fun idx v -> convertToCol m idx) h

let (mmult : matrix -> matrix -> matrix) = fun m1 m2 ->
  List.map (fun v -> List.map (dotprod v) (transpose m2)) m1

        
(* Problem 2: Calculators *)           
           
(* a type for arithmetic expressions *)
type op = Plus | Minus | Times | Divide
type exp = Num of float | BinOp of exp * op * exp

let eval = fun x1 x2 op ->
  match op with 
    Plus -> x1 +. x2
  | Minus -> x1 -. x2
  | Times -> x1 *. x2
  | Divide -> x1 /. x2

let rec (evalExp : exp -> float) = fun e ->
  match e with
    Num x -> x
  | BinOp (e1,operator,e2) -> let x1 = evalExp e1 in let x2 = evalExp e2 in eval x1 x2 operator

(* a type for stack instructions *)	  
type instr = Push of float | Swap | Calculate of op

let (execute : instr list -> float) = fun l ->
  let rec aux = fun instrList stack ->
    match instrList with
      [] -> stack
    | h::t -> match h with
                Push x -> aux t (x::stack)
              | Swap -> (match stack with
                          [] -> stack 
                        | _::[] -> stack
                        | h1::h2::tl1 -> aux t ([h2]@[h1]@tl1))
              | Calculate o -> (match stack with
                                  [] -> stack 
                                | _::[] -> stack
                                | a1::a2::tl2 -> aux t ((eval a2 a1 o)::tl2))
  in match aux l [] with
    [] -> 0.
  | ans::_ -> ans
      
let (compile : exp -> instr list) = fun expression ->
  let rec aux = fun e l ->
    match e with 
      Num x -> l @ [Push x]
    | BinOp(e1,operator,e2) -> l @ (aux e1 l) @ (aux e2 l) @ [Calculate operator]
  in aux expression []

let (decompile : instr list -> exp) = fun instructions ->
  let rec aux = fun instrList stack ->
    match instrList with
      [] -> stack
    | h::t -> match h with
                Push x -> aux t (Num x::stack)
              | Swap -> (match stack with
                          [] -> stack 
                        | _::[] -> stack
                        | h1::h2::tl1 -> aux t ([h2]@[h1]@tl1))
              | Calculate o -> (match stack with
                                  [] -> stack 
                                | _::[] -> stack
                                | a1::a2::tl2 -> aux t (BinOp(a2, o, a1)::tl2))
  in match aux instructions [] with
    [] -> Num 0.
  | result::_ -> result

(* EXTRA CREDIT *)
let compileRight = fun expression ->
  let rec aux = fun e l ->
    match e with
      Num x -> l @ [Push x]
    | BinOp(e1,operator,e2) -> if operator = Minus || operator = Divide then l @ (aux e2 l) @ (aux e1 l) @ [Swap] @ [Calculate operator]
                               else l @ (aux e2 l) @ (aux e1 l) @ [Calculate operator]
  in aux expression []

let getStackSize = fun instrList ->
  let rec aux = fun l curSize maxSize ->
    match l with
      [] -> maxSize
    | h::t -> (match h with
                Push x -> let newSize = curSize + 1 in if newSize > maxSize then (aux t newSize newSize) 
                                                       else (aux t newSize maxSize)
              | Swap -> (aux t curSize maxSize)
              | Calculate o -> (aux t (curSize - 1) maxSize))
  in aux instrList 0 0

let (compileOpt : exp -> (instr list * int)) = fun expression ->
  let leftInstr = (compile expression)
  in let rightInstr = (compileRight expression)
  in let leftSize = getStackSize leftInstr
  in let rightSize = getStackSize rightInstr
  in if leftSize <= rightSize then (leftInstr, leftSize)
     else (rightInstr, rightSize)