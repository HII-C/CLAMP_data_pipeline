package conditions;

public class RelationCondition implements ConditionIntf{
    @Override
	public String getSQLAddQuery() {
        return "mysqlquery";
    }
}