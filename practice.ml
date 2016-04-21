let rec replicate = fun l n ->
	let rec prepend = fun e n ->
		match n with
			0 -> []
			| _ -> e::(prepend e (n-1))
	in match l with 
		[] -> []
		| h::t -> match n with
					0 -> []
					| _ -> (prepend h n)@(replicate t n)

let rec drop = fun l n ->
	match l with
		[] -> []
		| h::t -> match n with
					0 -> l
					| 1 -> (drop t 0)
					| _ -> h::(drop t (n-1))

let rec slice = fun l start last ->
	match l with
		[] -> [];
		| h::t -> match start with
					1 -> match last with
							0 -> []
							| _ -> h::(slice t 1 (last-1))
					| _ -> (slice t (start-1) (last-1))

let rec rotateLeft = fun l n ->
	match l with
		[] -> []
		| h::t -> match n with
					0 -> l
					| _ -> (rotateLeft (t@[h]) (n-1))

let rec fold_left = fun f l ifNil ->
	match l with
		[] -> ifNil
		| [x] -> x
		| h1::h2::t -> (fold_left f ((f h1 h2)::t) ifNil)

let filter = fun f l ->
	List.fold_right (fun h t -> if (f h) then h::t else t) l []

let rec suffixes = fun l ->
	match l with
		[] -> []
		| [a] -> [[a]]
		| h::t -> l::(suffixes t)

let rec map_odd = fun f l ->
	match l with
		[] -> []
		| [a] -> (f a)::[]
		| h1::h2::t -> (f h1)::(map_odd f t)

let find_min = fun l ->
	List.fold_right (fun a b -> min a b) l max_int

let count_true_false = fun l ->
	List.fold_right (fun a b -> match b with 
									(x,y) -> if a then (x+1,y)
											 else (x,y+1)) l (0,0)

