package idv.kuma.app.komica.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by TakumaLee on 2016/5/2.
 */
public class FilterCollection {
    /**
     * Java Object Filter
     *
     * @return Collection
     * @author takumalee
     */


    public static <T> Collection<T> filter(Collection<T> target, PredicateInterface<T> predicate) {
        Collection<T> filteredCollection = new ArrayList<T>();
        try {
            for (T t : target) {
                if (predicate.apply(t)) {
                    filteredCollection.add(t);
                }
            }
        } catch (Exception e) {
        }

        return filteredCollection;
    }

    public interface PredicateInterface<T> {
        public boolean apply(T type);
    }
}
