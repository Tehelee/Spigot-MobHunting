package com.tehelee.mobHunting;

public class Pair<A, B> {
	public A a;
	public B b;

	public Pair(A a, B b) {
		super();
		this.a = a;
		this.b = b;
	}

	public int hashCode() {
		int hashFirst = a != null ? a.hashCode() : 0;
		int hashSecond = b != null ? b.hashCode() : 0;
		
		if ((a instanceof MobType) && (b instanceof Integer))
			return MobType.getId((MobType) a, (Integer) b).hashCode();

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	public boolean equals(Object other) {
		if (other instanceof Pair) {
			Pair<?, ?> otherPair = (Pair<?, ?>) other;
			return 
			((  this.a == otherPair.a ||
				( this.a != null && otherPair.a != null &&
				  this.a.equals(otherPair.a))) &&
			 (	this.b == otherPair.b ||
				( this.b != null && otherPair.b != null &&
				  this.b.equals(otherPair.b))) );
		}

		return false;
	}

	public String toString()
	{ 
		   return "(" + a + ", " + b + ")"; 
	}
}