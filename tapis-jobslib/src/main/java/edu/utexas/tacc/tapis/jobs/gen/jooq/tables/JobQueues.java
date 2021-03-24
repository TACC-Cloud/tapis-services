/*
 * This file is generated by jOOQ.
 */
package edu.utexas.tacc.tapis.jobs.gen.jooq.tables;


import edu.utexas.tacc.tapis.jobs.gen.jooq.Indexes;
import edu.utexas.tacc.tapis.jobs.gen.jooq.Keys;
import edu.utexas.tacc.tapis.jobs.gen.jooq.Public;
import edu.utexas.tacc.tapis.jobs.gen.jooq.tables.records.JobQueuesRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row7;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class JobQueues extends TableImpl<JobQueuesRecord> {

    private static final long serialVersionUID = 172694407;

    /**
     * The reference instance of <code>public.job_queues</code>
     */
    public static final JobQueues JOB_QUEUES = new JobQueues();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<JobQueuesRecord> getRecordType() {
        return JobQueuesRecord.class;
    }

    /**
     * The column <code>public.job_queues.id</code>.
     */
    public final TableField<JobQueuesRecord, Integer> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('job_queues_id_seq'::regclass)", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>public.job_queues.name</code>.
     */
    public final TableField<JobQueuesRecord, String> NAME = createField(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.job_queues.priority</code>.
     */
    public final TableField<JobQueuesRecord, Integer> PRIORITY = createField(DSL.name("priority"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.job_queues.filter</code>.
     */
    public final TableField<JobQueuesRecord, String> FILTER = createField(DSL.name("filter"), org.jooq.impl.SQLDataType.VARCHAR(4096).nullable(false), this, "");

    /**
     * The column <code>public.job_queues.uuid</code>.
     */
    public final TableField<JobQueuesRecord, String> UUID = createField(DSL.name("uuid"), org.jooq.impl.SQLDataType.VARCHAR(64).nullable(false), this, "");

    /**
     * The column <code>public.job_queues.created</code>.
     */
    public final TableField<JobQueuesRecord, LocalDateTime> CREATED = createField(DSL.name("created"), org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false).defaultValue(org.jooq.impl.DSL.field("timezone('utc'::text, now())", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.job_queues.last_updated</code>.
     */
    public final TableField<JobQueuesRecord, LocalDateTime> LAST_UPDATED = createField(DSL.name("last_updated"), org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false).defaultValue(org.jooq.impl.DSL.field("timezone('utc'::text, now())", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * Create a <code>public.job_queues</code> table reference
     */
    public JobQueues() {
        this(DSL.name("job_queues"), null);
    }

    /**
     * Create an aliased <code>public.job_queues</code> table reference
     */
    public JobQueues(String alias) {
        this(DSL.name(alias), JOB_QUEUES);
    }

    /**
     * Create an aliased <code>public.job_queues</code> table reference
     */
    public JobQueues(Name alias) {
        this(alias, JOB_QUEUES);
    }

    private JobQueues(Name alias, Table<JobQueuesRecord> aliased) {
        this(alias, aliased, null);
    }

    private JobQueues(Name alias, Table<JobQueuesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> JobQueues(Table<O> child, ForeignKey<O, JobQueuesRecord> key) {
        super(child, key, JOB_QUEUES);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.JOB_QUEUES_NAME_IDX, Indexes.JOB_QUEUES_PRIORITY_IDX, Indexes.JOB_QUEUES_UUID_IDX);
    }

    @Override
    public Identity<JobQueuesRecord, Integer> getIdentity() {
        return Keys.IDENTITY_JOB_QUEUES;
    }

    @Override
    public UniqueKey<JobQueuesRecord> getPrimaryKey() {
        return Keys.JOB_QUEUES_PKEY;
    }

    @Override
    public List<UniqueKey<JobQueuesRecord>> getKeys() {
        return Arrays.<UniqueKey<JobQueuesRecord>>asList(Keys.JOB_QUEUES_PKEY);
    }

    @Override
    public JobQueues as(String alias) {
        return new JobQueues(DSL.name(alias), this);
    }

    @Override
    public JobQueues as(Name alias) {
        return new JobQueues(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public JobQueues rename(String name) {
        return new JobQueues(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public JobQueues rename(Name name) {
        return new JobQueues(name, null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<Integer, String, Integer, String, String, LocalDateTime, LocalDateTime> fieldsRow() {
        return (Row7) super.fieldsRow();
    }
}
