package de.lmu.ifi.dbs.elki.database;

import de.lmu.ifi.dbs.elki.data.DatabaseObject;
import de.lmu.ifi.dbs.elki.distance.Distance;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.index.tree.metrical.MetricalIndex;
import de.lmu.ifi.dbs.elki.index.tree.metrical.MetricalNode;
import de.lmu.ifi.dbs.elki.index.tree.metrical.mtreevariants.MTreeEntry;
import de.lmu.ifi.dbs.elki.properties.Properties;
import de.lmu.ifi.dbs.elki.utilities.QueryResult;
import de.lmu.ifi.dbs.elki.utilities.UnableToComplyException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ClassParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.elki.utilities.pairs.SimplePair;

import java.util.ArrayList;
import java.util.List;

/**
 * MetricalIndexDatabase is a database implementation which is supported by a
 * metrical index structure.
 *
 * @author Elke Achtert
 * @param <O> the type of FeatureVector as element of the database
 */
public class MetricalIndexDatabase<O extends DatabaseObject, D extends Distance<D>, N extends MetricalNode<N, E>, E extends MTreeEntry<D>>
    extends IndexDatabase<O> {

    /**
     * OptionID for {@link #INDEX_PARAM}
     */
    public static final OptionID INDEX_ID = OptionID.getOrCreateOptionID(
        "metricalindexdb.index",
        "Classname of the metrical index to use " +
            Properties.ELKI_PROPERTIES.restrictionString(MetricalIndex.class) +
            ". "
    );

    /**
     * Parameter to specify the metrical index to use.
     * <p>Key: {@code -metricalindexdb.index} </p>
     */
    private final ClassParameter<MetricalIndex<O, D, N, E>> INDEX_PARAM =
      new ClassParameter<MetricalIndex<O, D, N, E>>(
        INDEX_ID, MetricalIndex.class);


    /**
     * The metrical index storing the data.
     */
    MetricalIndex<O, D, N, E> index;

    public MetricalIndexDatabase() {
        super();
        addOption(INDEX_PARAM);
    }

    /**
     * Calls the super method and afterwards inserts the specified object into
     * the underlying index structure.
     */
    @Override
    public Integer insert(SimplePair<O, Associations> objectAndAssociations) throws UnableToComplyException {
        Integer id = super.insert(objectAndAssociations);
        O object = objectAndAssociations.getFirst();
        index.insert(object);
        return id;
    }

    /**
     * Calls the super method and afterwards inserts the specified objects into
     * the underlying index structure. If the option bulk load is enabled and
     * the index structure is empty, a bulk load will be performed. Otherwise
     * the objects will be inserted sequentially.
     */
    @Override
    public void insert(List<SimplePair<O, Associations>> objectsAndAssociationsList) throws UnableToComplyException {
        for (SimplePair<O, Associations> objectAndAssociations : objectsAndAssociationsList) {
            super.insert(objectAndAssociations);
        }
        this.index.insert(getObjects(objectsAndAssociationsList));
    }

    @SuppressWarnings("unchecked")
    public <T extends Distance<T>> List<QueryResult<T>> rangeQuery(Integer id,
                                                                   String epsilon,
                                                                   DistanceFunction<O, T> distanceFunction) {
        if (!distanceFunction.getClass().equals(index.getDistanceFunction().getClass()))
            throw new IllegalArgumentException("Parameter distanceFunction must be an instance of " +
                index.getDistanceFunction().getClass());

        List<QueryResult<D>> rangeQuery = index.rangeQuery(get(id), epsilon);

        List<QueryResult<T>> result = new ArrayList<QueryResult<T>>();
        for (QueryResult<D> qr : rangeQuery)
            result.add((QueryResult<T>) qr);

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Distance<T>> List<QueryResult<T>> kNNQueryForObject(
        O queryObject, int k, DistanceFunction<O, T> distanceFunction) {
        if (!distanceFunction.getClass().equals(index.getDistanceFunction().getClass()))
            throw new IllegalArgumentException("Parameter distanceFunction must be an instance of "
                + index.getDistanceFunction().getClass());

        List<QueryResult<D>> knnQuery = index.kNNQuery(queryObject, k);

        List<QueryResult<T>> result = new ArrayList<QueryResult<T>>();
        for (QueryResult<D> qr : knnQuery)
            result.add((QueryResult<T>) qr);

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Distance<T>> List<QueryResult<T>> kNNQueryForID(Integer id, int k, DistanceFunction<O, T> distanceFunction) {

        if (!distanceFunction.getClass().equals(index.getDistanceFunction().getClass()))
            throw new IllegalArgumentException("Parameter distanceFunction must be an instance of "
                + index.getDistanceFunction().getClass());

        List<QueryResult<D>> knnQuery = index.kNNQuery(get(id), k);

        List<QueryResult<T>> result = new ArrayList<QueryResult<T>>();
        for (QueryResult<D> qr : knnQuery)
            result.add((QueryResult<T>) qr);

        return result;
    }

    public <T extends Distance<T>> List<List<QueryResult<T>>> bulkKNNQueryForID(@SuppressWarnings("unused") List<Integer> ids, @SuppressWarnings("unused") int k, @SuppressWarnings("unused") DistanceFunction<O, T> distanceFunction) {
        throw new UnsupportedOperationException("Not yet supported!");
    }

    @SuppressWarnings("unchecked")
    public <T extends Distance<T>> List<QueryResult<T>> reverseKNNQuery(Integer id, int k, DistanceFunction<O, T> distanceFunction) {
        if (!distanceFunction.getClass().equals(index.getDistanceFunction().getClass()))
            throw new IllegalArgumentException("Parameter distanceFunction must be an instance of "
                + index.getDistanceFunction().getClass() +
                ", but is " + distanceFunction.getClass());

        List<QueryResult<D>> rknnQuery = index.reverseKNNQuery(get(id), k);

        List<QueryResult<T>> result = new ArrayList<QueryResult<T>>();
        for (QueryResult<D> qr : rknnQuery)
            result.add((QueryResult<T>) qr);

        return result;
    }

    /**
     * Returns a string representation of this database.
     *
     * @return a string representation of this database.
     */
    @Override
    public String toString() {
        return index.toString();
    }

    /**
     * Returns the index of this database.
     *
     * @return the index of this database
     */
    @Override
    public MetricalIndex<O, D, N, E> getIndex() {
        return index;
    }

    /**
     * Sets the values for the parameter bulk.
     * If the parameters is not specified the default value is set.
     *
     */
    @Override
    public String[] setParameters(String[] args) throws ParameterException {
        String[] remainingParameters = super.setParameters(args);

        index = INDEX_PARAM.instantiateClass();

        remainingParameters = index.setParameters(remainingParameters);
        setParameters(args, remainingParameters);
        index.setDatabase(this);

        return remainingParameters;
    }

    /**
     * Returns a short description of the database.
     * (Such as: efficiency in space and time, index structure...)
     *
     * @return a description of the database
     */
    @Override
    public String parameterDescription() {
        StringBuffer description = new StringBuffer();
        description.append(this.getClass().getName());
        description.append(" holds all the data in a ");
        description.append("metrical index structure extending ").append(MetricalIndex.class.getName()).append(".\n");
//    description.append(index.getClass().getName()).append(" index structure.\n");
        description.append(optionHandler.usage("", false));
        return description.toString();
    }
}