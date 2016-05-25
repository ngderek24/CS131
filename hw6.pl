/* Name: Derek Nguyen

   UID: 304275956

   Others With Whom I Discussed Things: Sung Hyun Yoon	904303999

   Other Resources I Consulted:
   http://www.gprolog.org/manual/gprolog.html
   
*/

% Problem 1
duplist([], []).
duplist([X|T1], [X,X|T2]) :- duplist(T1, T2).


% Problem 2
subseq([], []).
subseq([], [_|_]).
subseq([X|T1], [X|T2]) :- subseq(T1, T2).
subseq([X|T1], [_|T2]) :- subseq([X|T1], T2).


% Problem 3
verbalArithHelper([], [], [], 0).
verbalArithHelper([], [], [H|T], Carry) :- member(H, [0,1,2,3,4,5,6,7,8,9]),
										   H is Carry,
										   verbalArithHelper([], [], T, 0).

verbalArithHelper([], [S|T2], [T|T3], Carry) :- member(S, [0,1,2,3,4,5,6,7,8,9]),
												member(T, [0,1,2,3,4,5,6,7,8,9]),
												T is (S + Carry) mod 10,
												NewCarry is floor((S + Carry)/10),
												verbalArithHelper([], T2, T3, NewCarry).

verbalArithHelper([F|T1], [], [T|T3], Carry) :- member(F, [0,1,2,3,4,5,6,7,8,9]),
												member(T, [0,1,2,3,4,5,6,7,8,9]),
												T is (F + Carry) mod 10,
												NewCarry is floor((F + Carry)/10),														
												verbalArithHelper(T1, [], T3, NewCarry).

verbalArithHelper([O1|T1], [O2|T2], [Sum|T3], Carry) :- member(O1, [0,1,2,3,4,5,6,7,8,9]), 
											 			member(O2, [0,1,2,3,4,5,6,7,8,9]), 
											 			member(Sum, [0,1,2,3,4,5,6,7,8,9]),
														Sum is (O1 + O2 + Carry) mod 10,
														NewCarry is floor((O1 + O2 + Carry)/10),
														verbalArithHelper(T1, T2, T3, NewCarry).

verbalarithmetic(Letters, [F|T1], [S|T2], [T|T3]) :- member(F, [1,2,3,4,5,6,7,8,9]),
													 member(S, [1,2,3,4,5,6,7,8,9]),
													 member(T, [1,2,3,4,5,6,7,8,9]),
													 reverse([F|T1], FirstRev),
													 reverse([S|T2], SecondRev),
													 reverse([T|T3], ThirdRev),
													 verbalArithHelper(FirstRev, SecondRev, ThirdRev, 0),
												   	 fd_all_different(Letters).


% Problem 4
move(world([O|S], Stack2, Stack3, none), pickup(O, stack1), world(S, Stack2, Stack3, O)).
move(world(Stack1, [O|S], Stack3, none), pickup(O, stack2), world(Stack1, S, Stack3, O)).
move(world(Stack1, Stack2, [O|S], none), pickup(O, stack3), world(Stack1, Stack2, S, O)).

move(world(S, Stack2, Stack3, O), putdown(O, stack1), world([O|S], Stack2, Stack3, none)).
move(world(Stack1, S, Stack3, O), putdown(O, stack2), world(Stack1, [O|S], Stack3, none)).
move(world(Stack1, Stack2, S, O), putdown(O, stack3), world(Stack1, Stack2, [O|S], none)).

blocksworld(Goal, [], Goal).
blocksworld(Start, [pickup(B, S)|Moves], Goal) :- move(Start, pickup(B, S), Result), blocksworld(Result, Moves, Goal).
blocksworld(Start, [putdown(B, S)|Moves], Goal) :- move(Start, putdown(B, S), Result), blocksworld(Result, Moves, Goal).