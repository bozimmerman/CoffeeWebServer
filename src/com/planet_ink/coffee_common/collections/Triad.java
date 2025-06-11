package com.planet_ink.coffee_common.collections;

/*
   Copyright 2010-2025 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
public class Triad<T,K,L> extends Pair<T,K>
{
	private static final long serialVersionUID = 3227647705379969966L;
	public L third;

	public Triad(final T frst, final K scnd, final L thrd)
	{
		super(frst,scnd);
		third=thrd;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
			return true;
		if (o instanceof Triad)
		{
			final Triad<?,?,?> p = (Triad<?,?,?>) o;
			return ((p.first == first) || ((p.first != null) && (p.first.equals(first)))) && ((p.second == second) || ((p.second != null) && (p.second.equals(second))))
					&& ((p.third == third) || ((p.third != null) && (p.third.equals(third))));
		}
		return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ ((third == null) ? 0 : third.hashCode());
	}
}
