package helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import interfaces.Predicate;

/*
 * class definition for SmartList objects
 */
public class SmartList<T> extends ArrayList<T> {

	/*
	 * constructor method for SmartList Objects
	 */
	public SmartList(){
		
	}
	
	/*
	 * alternate constructor for SmartList objects
	 */
	public SmartList(Collection<T> sl){
		super(sl);
	}
	
	/*
	 * generalized filter function
	 */
	public SmartList<T> where(Predicate<T> p) throws Exception{
		SmartList<T> result = new SmartList<>();
		for(T e: this){
			if(p.test(e)){
				result.add(e);
			}
		}
		return result;
	}
	
	/*
	 * generalized remove duplicate function
	 */
	public void removeDuplicates(Comparator<T> comp){
		
		ListIterator<T> iter = this.listIterator();
		
		for(T item:this){	
			while(iter.hasNext()){
				T current = iter.next();
				// ignore self (first instance)
				if(item == current)
					continue;
				// if duplicate is detected
				else if(comp.compare(item, current) == 0)
					// remove current
					iter.remove();
			}
		}
	}
	
}
