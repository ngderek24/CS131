(*
  Derek Nguyen
  304275956

  Discussed problems with
  Sung Hyun Yoon 904303999
*)

(* Problem 1 *)
      
let rec (member : 'a -> 'a list -> bool) =
  (function x ->
    (function s -> 
      match s with
        [] -> false
      | h::t -> h=x || member x t
    )
  )

let (add : 'a -> 'a list -> 'a list) =
  (function x ->
    (function s ->
      if (member x s) then s
      else x::s
    )
  )

let rec (union : 'a list -> 'a list -> 'a list) =
  (function s1 ->
    (function s2 ->
      match s1 with
        [] -> s2
      | h::t -> (union t (add h s2))
    )
  )

let rec (fastUnion : 'a list -> 'a list -> 'a list) =
  (function s1 ->
    (function s2 ->
      match (s1, s2) with
        ([], []) -> []
      | (h::t, []) -> s1
      | ([], h::t) -> s2
      | (h1::t1, h2::t2) -> if h1 < h2 then h1::(fastUnion t1 s2)
                            else if h1 = h2 then h1::(fastUnion t1 t2)
                            else h2::(fastUnion s1 t2) 
    )
  )
    
let (intersection : 'a list -> 'a list -> 'a list) =
  (function s1 ->
    (function s2 ->
      List.filter (function x -> member x s2) s1
    )
  )
               
let rec (setify : 'a list -> 'a list) =
  (function l ->
    match l with
      [] -> []
    | h::t -> (add h (setify t))
  )

let rec (powerset : 'a list -> 'a list list) =
  (function s ->
    match s with
      [] -> [[]]
    | h::t -> let ps = (powerset t) in (ps @ (List.map (function x -> [h] @ x) ps))
  )

       
(* Problem 2 *)        
        
let rec (partition : ('a -> bool) -> 'a list -> 'a list * 'a list) =
  (function fxn -> 
    (function lst ->
      match lst with
        [] -> ([], [])
      | h::t -> if fxn h then match (partition fxn t) with
                                (f, s) -> (h::f, s)
                else match (partition fxn t) with
                      (f, s) -> (f, h::s)
    )
  )

let rec (whle : ('a -> bool) -> ('a -> 'a) -> 'a -> 'a) =
  (function loopCond ->
    (function fxn ->
      (function x ->
        if loopCond x then (whle loopCond fxn (fxn x))
        else x
      )
    )
  )
                                     
let rec (pow : int -> ('a -> 'a) -> ('a -> 'a)) =
  (function n ->
    (function f ->
      (function v ->
        match n with
          0 -> v
        | _ -> (f (pow (n-1) f v))
      )
    )
  )