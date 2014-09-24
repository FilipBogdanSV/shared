/**
 *	Copyright (C) Miklos Maroti, 2000-2004
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 2 of the License, or (at your 
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.mmaroti.ua.alg;

import org.mmaroti.ua.util.*;

import java.util.*;

/**
 * This structure represents the (absolutely free) term algebra over a finite
 * set of variables. The elements of the term algebra are not enumerated, even
 * if there are only constant operations.
 * 
 * @author mmaroti@math.u-szeged.hu
 */
public class TermAlgebra extends Algebra {
	/**
	 * This method always throws an exception, because the elements are not
	 * enumerated (even if there are only constant operations).
	 * 
	 * @throws UnsupportedOperationException
	 */
	public int getSize() {
		throw new UnsupportedOperationException("all terms are not enumerable");
	}

	/**
	 * This method always throws an {@link UnsupportedOperationException}
	 * exception.
	 * 
	 * @see #getSize()
	 */
	public int getIndex(Object element) {
		throw new UnsupportedOperationException("all terms are not enumerable");
	}

	/**
	 * This method always throws an {@link UnsupportedOperationException}
	 * exception, because the elements of term algebras are not enumerated.
	 * 
	 * @see #getSize()
	 */
	public Object getElement(int index) {
		throw new UnsupportedOperationException("all terms are not enumerable");
	}

	public boolean areEquals(Object a, Object b) {
		return a.equals(b);
	}

	public int hashCode(Object element) {
		return element.hashCode();
	}

	public String toString(Object element) {
		return element.toString();
	}

	public Object parse(String string) {
		return Term.parse(TermAlgebra.this, string);
	}

	/**
	 * Constructs an empty term algebra.
	 */
	protected TermAlgebra() {
	}

	/**
	 * Constructs a term algebra with the specified list of operation symbols.
	 */
	public TermAlgebra(Signature signature) {
		operations = new Op[signature.operations.length];

		for (int i = 0; i < operations.length; ++i) {
			Symbol symbol = signature.operations[i];
			this.operations[i] = new Op(symbol);
		}
	}

	protected Op[] operations;

	public final Operation[] getOperations() {
		return operations;
	}

	public class Term {
		/**
		 * Constructs a term whose topmost operation is indexed by
		 * <code>index</code> and subterms are <code>subterms</code>. If the
		 * index is non-negative, then this term represents a variable. If it is
		 * negative, then it is the index of the operation of the underlying
		 * algebra.
		 */
		protected Term(int index, Term[] subterms) {
			assert (index < 0 || subterms == null);

			this.index = index;
			this.subterms = subterms;
		}

		protected final int index;

		/**
		 * Returns the topmost operation of this term. If this term is a
		 * variable then this method returns a non-negative index. Otherwise the
		 * returned value is <code>-(i+1)</code> which corresponds to the ith
		 * operation.
		 */
		public final int getIndex() {
			return index;
		}

		protected final Term[] subterms;

		/**
		 * Returns the subterms of this term. If this term is a variable then
		 * this method returns <code>null</code>.
		 */
		public final Term[] getSubterms() {
			return subterms;
		}

		/**
		 * Returns the length of this term (the number of variables plus the
		 * number of operations). The length is always positive.
		 */
		public int getLength() {
			int length = 1;

			if (subterms != null)
				for (int i = 0; i < subterms.length; ++i)
					length += subterms[i].getLength();

			return length;
		}

		/**
		 * Returns the depth of this term. This number is always non-negative.
		 * The depth of variables and constants is zero.
		 */
		public int getDepth() {
			int depth = 0;

			if (subterms != null)
				for (int i = 0; i < subterms.length; ++i) {
					int d = subterms[i].getDepth();
					if (d > depth)
						depth = d;
				}

			return depth + 1;
		}

		/**
		 * Returns the number of occurrences of a subterm.
		 */
		public int getNumberOfOccurences(Term subterm) {
			if (subterm == this)
				return 1;
			else if (subterms == null)
				return 0;

			int occurences = 0;
			for (int i = 0; i < subterms.length; ++i)
				occurences += subterms[i].getNumberOfOccurences(subterm);

			return occurences;
		}

		protected String subtermToString(int index) {
			if (symbol.isBraced(index, subterms[index].symbol))
				return "(" + subterms[index].toString() + ")";
			else
				return subterms[index].toString();
		}

		public String toString() {
			if (index >= 0)
				return "x" + index;

			Symbol symbol = operations[-index - 1].symbol;

			if (symbol.hasProperty(Symbol.INFIX)) {
				if (subterms.length == 0)
					return symbol.getName();
				else if (subterms.length == 1)
					return symbol.getName() + subtermToString(0);
				else if (subterms.length == 2)
					return subtermToString(0) + symbol.getName()
							+ subtermToString(1);
			}

			String s = symbol.getName() + '(';
			for (int i = 0; i < subterms.length; ++i) {
				if (i > 0)
					s += ",";

				s += subterms[i];
			}
			return s + ')';
		}

		/**
		 * Returns <code>true</code> if this term is a variable,
		 * <code>false</code> otherwise.
		 */
		public boolean isVariable() {
			return index >= 0;
		}

		/**
		 * This function returns the set of variables of this term.
		 */
		public Set<Integer> getVariables() {
			HashSet<Integer> variables = new HashSet<Integer>();
			addMyVariablesTo(variables);
			return variables;
		}

		/**
		 * This function adds the generators of this term to the set of
		 * generators stored in <code>set</code>.
		 */
		public void addMyVariablesTo(Collection<Integer> collection) {
			if (!isVariable()) {
				for (int i = 0; i < subterms.length; ++i)
					subterms[i].addMyVariablesTo(collection);
			} else
				collection.add(index);
		}

		/**
		 * Checks if this term contains the specified subterm.
		 * 
		 * @param subterm
		 *            another term of this algebra
		 * @return <code>true</code> if <code>subterm</code> is a subterm of
		 *         this term.
		 */
		public boolean hasSubterm(Term subterm) {
			if (subterm == this)
				return true;

			for (int i = 0; i < subterms.length; ++i)
				if (subterms[i].hasSubterm(subterm))
					return true;

			return false;
		}

		public int hashCode() {
			int hashcode = index;

			int i = subterms.length;
			while (--i >= 0) {
				hashcode *= 1973;
				hashcode += subterms[i].hashCode();
			}

			return hashcode;
		}

		public boolean equals(Object object) {
			Term other = (Term) object;

			if (index != other.index)
				return false;

			assert (subterms.length == other.subterms.length);

			int i = subterms.length;
			while (--i >= 0)
				if (!subterms[i].equals(other.subterms[i]))
					return false;

			return true;
		}
	}

	protected static Parser parser = new Parser();

	private static boolean bracedLastParse;

	protected static Term parseSubterm(Symbol symbol, int index,
			TermAlgebra algebra, String substring) {
		Term term = parse(algebra, substring);

		if (term != null && symbol.isBraced(index, term.symbol)
				&& !bracedLastParse)
			term = null;

		return term;
	}

	public static Term parse(TermAlgebra algebra, String string) {
		string = string.trim();

		if (string.startsWith("(") && string.endsWith(")")) {
			Term term = parse(algebra, string.substring(1, string.length() - 1));
			if (term != null) {
				bracedLastParse = true;
				return term;
			}
		}

		Symbol symbol = (Symbol) Symbol.VARIABLES.parse(string);
		if (symbol != null) {
			bracedLastParse = false;
			return new Term(symbol, new Term[0]);
		}

		Operation[] ops = algebra.getOperations();
		for (int i = 0; i < ops.length; ++i) {
			symbol = ops[i].getSymbol();
			String name = symbol.getName();

			if (symbol.hasProperty(Symbol.INFIX)) {
				if (symbol.getArity() == 0 && string.equals(name)) {
					bracedLastParse = false;
					return new Term(symbol, new Term[0]);
				} else if (symbol.getArity() == 1 && string.startsWith(name)) {
					Term subterm = parseSubterm(symbol, 0, algebra,
							string.substring(name.length()));

					if (subterm != null) {
						bracedLastParse = false;
						return new Term(symbol, new Term[] { subterm });
					}
				} else if (symbol.getArity() == 2) {
					int pos = -1;
					for (;;) {
						pos = parser.indexOf(string, name, pos + 1);
						if (pos < 0)
							break;

						Term subterm1 = parseSubterm(symbol, 0, algebra,
								string.substring(0, pos));
						Term subterm2 = parseSubterm(symbol, 1, algebra,
								string.substring(pos + name.length()));

						if (subterm1 != null && subterm2 != null) {
							bracedLastParse = false;
							return new Term(symbol, new Term[] { subterm1,
									subterm2 });
						}
					}
				}
			}

			if (string.startsWith(name + "(") && string.endsWith(")")) {
				String[] substrings = parser.parseList(string.substring(
						name.length() + 1, string.length() - 1), ",");
				if (substrings != null && substrings.length == symbol.arity) {
					Term[] subterms = new Term[substrings.length];

					int j = subterms.length;
					while (--j >= 0)
						if ((subterms[j] = parse(algebra, substrings[j])) == null)
							break;

					if (j < 0)
						return new Term(symbol, subterms);
				}
			}
		}

		return null;
	}

	/**
	 * A basic operation of a term algebra.
	 */
	protected class Op extends Operation {
		protected final Symbol symbol;
		protected final int index;

		public Symbol getSymbol() {
			return symbol;
		}

		public int getIndex() {
			return index;
		}

		/**
		 * Constructs a new Operation.
		 * 
		 * @param name
		 *            The name of the operation.
		 * @param arity
		 *            The arity of the operation. This must be non-negative.
		 */
		public Op(Symbol symbol, int index) {
			this.symbol = symbol;
			this.index = index;
		}

		/**
		 * This method always throws an {@link UnsupportedOperationException}
		 * exception, since the elements are denumerable.
		 * 
		 * @see #getSize()
		 */
		public int getValue(int[] args) {
			throw new UnsupportedOperationException(
					"the elements of a term algebra cannot be enumerated");
		}

		/**
		 * Returns the {@link TermAlgebra.Term} whose topmost operation is
		 * <code>this</code> and arguments are <code>args</code>. We copy the
		 * passed argument array, so <code>args</code> can be freely used after
		 * this call.
		 */
		public Object getValue(Object[] args) {
			Term[] subterms = new Term[symbol.arity];

			System.arraycopy(args, 0, subterms, 0, subterms.length);
			// for(int i = 0; i < symbol.arity; ++i)
			// subterms[i] = (Term)args[i];

			return new Term(index, subterms);
		}

		/**
		 * Returns the {@link TermAlgebra.Term}whose topmost operation is
		 * <code>this</code> and arguments are <code>args</code>. We do NOT copy
		 * the passed argument array, so <code>args</code> should not be
		 * modified after this call.
		 */
		public Term getValue(Term[] args) {
			return new Term(index, args);
		}

		public int getSize() {
			return getSize();
		}
	}

	/**
	 * Returns the variable with the specified index.
	 */
	public Term getVariable(int index) {
		assert (index >= 0);

		return new Term(index, null);
	}

	/**
	 * Returns the list of first <code>count</code> many terms in increasing
	 * complexity generated by the first <code>vars</code> many variables.
	 */
	public List<Term> getSmallTerms(int count, int vars) {
		ArrayList<Term> terms = new ArrayList<Term>();
		if (count <= 0)
			return terms;

		// add the constants
		for (int i = 0; i < operations.length; ++i)
			if (operations[i].getArity() == 0) {
				terms.add((Term) operations[i].getValue(new Object[0]));
				if (--count <= 0)
					return terms;
			}

		// add the variables
		for (int i = 0; i < vars; ++i) {
			terms.add(getVariable(i));
			if (--count <= 0)
				return terms;
		}

		if (terms.size() == vars + operations.length)
			throw new IllegalArgumentException(
					"There are no non-constant operations");
		else if (terms.size() == 0)
			throw new IllegalArgumentException(
					"There are no constants or variables");

		// add more complex terms
		int level = 0;
		for (;;) {
			++level;
			for (int op = 0; op < operations.length; ++op) {
				if (operations[op].getArity() == 0)
					continue;

				Argument args = new SphereArgument(operations[op].getArity(),
						level);
				Object[] objs = new Object[operations[op].getArity()];

				if (args.reset())
					do {
						for (int i = 0; i < args.vector.length; ++i)
							objs[i] = terms.get(args.vector[i]);

						terms.add((Term) operations[op].getValue(objs));
						if (--count <= 0)
							return terms;
					} while (args.next());
			}
		}
	}

	/**
	 * Returns the identity endomorphism of this term algebra.
	 */
	public Evaluation createEndomorphism() {
		return new Evaluation(this, this);
	}

	/**
	 * Creates an endomorphism that maps the specified generator to the
	 * specified term and maps all other variables to themselves.
	 */
	public Evaluation createEndomorphism(Object generator, Term image) {
		Evaluation end = new Evaluation(this, this);

		end.map.put(generator, image);

		return end;
	}

	/**
	 * Returns the minimal endomorphism that maps the source term to the target
	 * term, or <code>null</code> is no such endomorphism exists.
	 */
	public Evaluation createExtension(Term source, Term target) {
		Evaluation end = new Evaluation(this, this);

		if (end.extend(source, target))
			return end;

		return null;
	}

	/**
	 * Finds the smallest endomorphism that maps the terms <code>a</code> and
	 * <code>b</code> to the same term. By smallest we mean that every other
	 * such endomorphism can be represented as the minimal one composed with an
	 * arbitrary endomorphism. If no such endomorphism can collapse the two
	 * terms, then <code>null</code> is returned.
	 */
	public Evaluation findCommonExtension(Term a, Term b) {
		Evaluation end = new Evaluation(this, this);

		if (areEquals(a, b))
			return end;
		else if (a.isVariable()) {
			if (b.hasSubterm(a))
				return null;

			end.set(a.symbol, b);
			return end;
		} else if (b.isVariable()) {
			if (a.hasSubterm(b))
				return null;

			end.set(b.symbol, a);
			return end;
		}

		if (a.symbol != b.symbol)
			return null;

		for (int i = 0; i < a.subterms.length; ++i) {
			Evaluation e = findCommonExtension(
					(Term) end.getValue(a.subterms[i]),
					(Term) end.getValue(b.subterms[i]));

			if (e == null)
				return null;

			end.compose(e);
		}

		return end;
	}

	/**
	 * Creates a new endomorphism of this term algebra that renames the
	 * variables from <code>variables</code> so that the new variables are not
	 * in the collection <code>avoid</code>. The variable collection can contain
	 * the same variable multiple times.
	 * 
	 * @throws IllegalArgumentException
	 *             if not enough variables are available to rename all variables
	 */
	public Evaluation renameVariables(Collection<Symbol> variables,
			Collection<Symbol> avoid) {
		Evaluation endomorphism = new Evaluation(this, this);

		Iterator<Symbol> iter = variables.iterator();
		int i = -1;
		while (iter.hasNext()) {
			Symbol var = iter.next();

			if (endomorphism.get(var) != null)
				continue;

			do
				++i;
			while (avoid.contains(Symbol.getVariable(i)));

			endomorphism.set(var, getVariable(i));
		}

		return endomorphism;
	}

	/**
	 * Renames the variables of this term so that it has no common variables of
	 * the specified collection of variables and the indices of the new
	 * variables are small.
	 */
	public Term renameVariables(Term term, Collection<Symbol> avoid) {
		HashSet<Symbol> variables = new HashSet<Symbol>();
		term.addMyVariablesTo(variables);

		Evaluation endomorphism = renameVariables(variables, avoid);
		return (Term) endomorphism.getValue(term);
	}

	/**
	 * Checks if a term is a specialization of one of the terms in a collection.
	 * By specialization we mean that there exists an endomorphism of the term
	 * algebra that maps one of the terms in the collection to the target term.
	 * 
	 * @param target
	 *            the term that is checked against all terms in the collection.
	 * @param terms
	 *            the collection containing (sample) terms.
	 * @return <code>true</code> if the specified term is a specialization of
	 *         one of the terms in the collection, <code>false</code> otherwise.
	 */
	public boolean isSpecialization(Term target, Collection<Term> terms) {
		Evaluation e = createEndomorphism();

		Iterator<Term> iter = terms.iterator();
		while (iter.hasNext()) {
			if (e.extend(iter.next(), target))
				return true;

			e.clear();
		}

		return false;
	}

	Relation[] relations = new Relation[0];

	public Relation[] getRelations() {
		return relations;
	}
}
