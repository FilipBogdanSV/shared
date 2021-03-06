package mmaroti.ua.alg;

/**
 *	Copyright (C) 2001 Miklos Maroti
 */

import java.util.*;
import mmaroti.ua.util.*;

public class Equivalence {
	protected int[] repr;

	public int size() {
		return repr.length;
	}

	public int[] reprezentation() {
		return repr;
	}

	public int reprezentative(int a) {
		return repr[a];
	}

	public boolean related(int a, int b) {
		return repr[a] == repr[b];
	}

	public int blockCount() {
		int c = 0;

		for (int i = 0; i < repr.length; ++i)
			if (repr[i] == i)
				++c;

		return c;
	}

	public int blockSize(int a) {
		int c = 0;
		a = repr[a];

		for (int i = 0; i < repr.length; ++i)
			if (repr[i] == a)
				++c;

		return c;
	}

	public void setZero() {
		for (int i = 0; i < repr.length; ++i)
			repr[i] = i;
	}

	public boolean isZero() {
		return blockCount() == size();
	}

	public void setOne() {
		Arrays.fill(repr, 0);
	}

	public boolean isOne() {
		return blockCount() == 1;
	}

	public void join(int a, int b) {
		a = repr[a];
		b = repr[b];

		if (a < b) {
			for (int i = b; i < repr.length; ++i)
				if (repr[i] == b)
					repr[i] = a;
		} else if (b < a) {
			for (int i = a; i < repr.length; ++i)
				if (repr[i] == a)
					repr[i] = b;
		}
	}

	public void join(Equivalence equ) {
		if (equ.repr.length != repr.length)
			throw new IllegalArgumentException();

		for (int i = 0; i < repr.length; ++i)
			join(i, equ.repr[i]);
	}

	public void meet(Equivalence equ) {
		int size = repr.length;
		int[] repr2 = equ.repr;

		if (size != repr2.length)
			throw new IllegalArgumentException();

		HashMap<IntPair, Integer> map = new HashMap<IntPair, Integer>();
		IntPair pair = new IntPair();

		for (int i = 0; i < size; ++i) {
			pair.first = repr[i];
			pair.second = repr2[i];

			Integer n = map.get(pair);
			if (n == null) {
				repr[i] = i;
				map.put(new IntPair(pair), i);
			} else
				repr[i] = n;
		}
	}

	public Equivalence(int size) {
		repr = new int[size];
	}

	protected Equivalence(int[] repr) {
		this.repr = repr;
	}

	@Override
	public Object clone() {
		return new Equivalence(repr.clone());
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Equivalence))
			return false;

		Equivalence e = (Equivalence) o;

		return Arrays.equals(repr, e.repr);
	}

	@Override
	public int hashCode() {
		int a = 0;

		for (int i = 0; i < repr.length; ++i) {
			if (repr[i] == i)
				a += repr.length;

			a += repr[i] ^ i;
		}

		return a;
	}

	public static Equivalence zero(int size) {
		Equivalence equ = new Equivalence(size);
		equ.setZero();
		return equ;
	}

	public static Equivalence one(int size) {
		Equivalence equ = new Equivalence(size);
		equ.setOne();
		return equ;
	}

	public static Equivalence join(Equivalence a, Equivalence b) {
		Equivalence equ = (Equivalence) a.clone();
		equ.join(b);
		return equ;
	}

	public static Equivalence meet(Equivalence a, Equivalence b) {
		Equivalence equ = (Equivalence) a.clone();
		equ.meet(b);
		return equ;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("[ ");

		for (int i = 0; i < repr.length; ++i) {
			if (repr[i] == i) {
				if (i != 0)
					s.append(" | ");

				s.append(i);
				for (int j = i + 1; j < repr.length; ++j)
					if (repr[j] == i) {
						s.append(' ');
						s.append(j);
					}
			}
		}

		s.append(" ]");
		return s.toString();
	}
}
