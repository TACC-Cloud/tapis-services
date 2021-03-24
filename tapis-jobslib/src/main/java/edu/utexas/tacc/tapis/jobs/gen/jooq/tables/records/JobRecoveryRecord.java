/*
 * This file is generated by jOOQ.
 */
package edu.utexas.tacc.tapis.jobs.gen.jooq.tables.records;


import edu.utexas.tacc.tapis.jobs.gen.jooq.tables.JobRecovery;

import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record12;
import org.jooq.Row12;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class JobRecoveryRecord extends UpdatableRecordImpl<JobRecoveryRecord> implements Record12<Integer, String, String, String, String, String, String, Integer, LocalDateTime, LocalDateTime, LocalDateTime, String> {

    private static final long serialVersionUID = -715536778;

    /**
     * Setter for <code>public.job_recovery.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.job_recovery.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.job_recovery.tenant_id</code>.
     */
    public void setTenantId(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.job_recovery.tenant_id</code>.
     */
    public String getTenantId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.job_recovery.condition_code</code>.
     */
    public void setConditionCode(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.job_recovery.condition_code</code>.
     */
    public String getConditionCode() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.job_recovery.tester_type</code>.
     */
    public void setTesterType(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.job_recovery.tester_type</code>.
     */
    public String getTesterType() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.job_recovery.tester_parms</code>.
     */
    public void setTesterParms(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.job_recovery.tester_parms</code>.
     */
    public String getTesterParms() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.job_recovery.policy_type</code>.
     */
    public void setPolicyType(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.job_recovery.policy_type</code>.
     */
    public String getPolicyType() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.job_recovery.policy_parms</code>.
     */
    public void setPolicyParms(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.job_recovery.policy_parms</code>.
     */
    public String getPolicyParms() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.job_recovery.num_attempts</code>.
     */
    public void setNumAttempts(Integer value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.job_recovery.num_attempts</code>.
     */
    public Integer getNumAttempts() {
        return (Integer) get(7);
    }

    /**
     * Setter for <code>public.job_recovery.next_attempt</code>.
     */
    public void setNextAttempt(LocalDateTime value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.job_recovery.next_attempt</code>.
     */
    public LocalDateTime getNextAttempt() {
        return (LocalDateTime) get(8);
    }

    /**
     * Setter for <code>public.job_recovery.created</code>.
     */
    public void setCreated(LocalDateTime value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.job_recovery.created</code>.
     */
    public LocalDateTime getCreated() {
        return (LocalDateTime) get(9);
    }

    /**
     * Setter for <code>public.job_recovery.last_updated</code>.
     */
    public void setLastUpdated(LocalDateTime value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.job_recovery.last_updated</code>.
     */
    public LocalDateTime getLastUpdated() {
        return (LocalDateTime) get(10);
    }

    /**
     * Setter for <code>public.job_recovery.tester_hash</code>.
     */
    public void setTesterHash(String value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.job_recovery.tester_hash</code>.
     */
    public String getTesterHash() {
        return (String) get(11);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record12 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row12<Integer, String, String, String, String, String, String, Integer, LocalDateTime, LocalDateTime, LocalDateTime, String> fieldsRow() {
        return (Row12) super.fieldsRow();
    }

    @Override
    public Row12<Integer, String, String, String, String, String, String, Integer, LocalDateTime, LocalDateTime, LocalDateTime, String> valuesRow() {
        return (Row12) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return JobRecovery.JOB_RECOVERY.ID;
    }

    @Override
    public Field<String> field2() {
        return JobRecovery.JOB_RECOVERY.TENANT_ID;
    }

    @Override
    public Field<String> field3() {
        return JobRecovery.JOB_RECOVERY.CONDITION_CODE;
    }

    @Override
    public Field<String> field4() {
        return JobRecovery.JOB_RECOVERY.TESTER_TYPE;
    }

    @Override
    public Field<String> field5() {
        return JobRecovery.JOB_RECOVERY.TESTER_PARMS;
    }

    @Override
    public Field<String> field6() {
        return JobRecovery.JOB_RECOVERY.POLICY_TYPE;
    }

    @Override
    public Field<String> field7() {
        return JobRecovery.JOB_RECOVERY.POLICY_PARMS;
    }

    @Override
    public Field<Integer> field8() {
        return JobRecovery.JOB_RECOVERY.NUM_ATTEMPTS;
    }

    @Override
    public Field<LocalDateTime> field9() {
        return JobRecovery.JOB_RECOVERY.NEXT_ATTEMPT;
    }

    @Override
    public Field<LocalDateTime> field10() {
        return JobRecovery.JOB_RECOVERY.CREATED;
    }

    @Override
    public Field<LocalDateTime> field11() {
        return JobRecovery.JOB_RECOVERY.LAST_UPDATED;
    }

    @Override
    public Field<String> field12() {
        return JobRecovery.JOB_RECOVERY.TESTER_HASH;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getTenantId();
    }

    @Override
    public String component3() {
        return getConditionCode();
    }

    @Override
    public String component4() {
        return getTesterType();
    }

    @Override
    public String component5() {
        return getTesterParms();
    }

    @Override
    public String component6() {
        return getPolicyType();
    }

    @Override
    public String component7() {
        return getPolicyParms();
    }

    @Override
    public Integer component8() {
        return getNumAttempts();
    }

    @Override
    public LocalDateTime component9() {
        return getNextAttempt();
    }

    @Override
    public LocalDateTime component10() {
        return getCreated();
    }

    @Override
    public LocalDateTime component11() {
        return getLastUpdated();
    }

    @Override
    public String component12() {
        return getTesterHash();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getTenantId();
    }

    @Override
    public String value3() {
        return getConditionCode();
    }

    @Override
    public String value4() {
        return getTesterType();
    }

    @Override
    public String value5() {
        return getTesterParms();
    }

    @Override
    public String value6() {
        return getPolicyType();
    }

    @Override
    public String value7() {
        return getPolicyParms();
    }

    @Override
    public Integer value8() {
        return getNumAttempts();
    }

    @Override
    public LocalDateTime value9() {
        return getNextAttempt();
    }

    @Override
    public LocalDateTime value10() {
        return getCreated();
    }

    @Override
    public LocalDateTime value11() {
        return getLastUpdated();
    }

    @Override
    public String value12() {
        return getTesterHash();
    }

    @Override
    public JobRecoveryRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value2(String value) {
        setTenantId(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value3(String value) {
        setConditionCode(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value4(String value) {
        setTesterType(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value5(String value) {
        setTesterParms(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value6(String value) {
        setPolicyType(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value7(String value) {
        setPolicyParms(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value8(Integer value) {
        setNumAttempts(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value9(LocalDateTime value) {
        setNextAttempt(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value10(LocalDateTime value) {
        setCreated(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value11(LocalDateTime value) {
        setLastUpdated(value);
        return this;
    }

    @Override
    public JobRecoveryRecord value12(String value) {
        setTesterHash(value);
        return this;
    }

    @Override
    public JobRecoveryRecord values(Integer value1, String value2, String value3, String value4, String value5, String value6, String value7, Integer value8, LocalDateTime value9, LocalDateTime value10, LocalDateTime value11, String value12) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        value12(value12);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached JobRecoveryRecord
     */
    public JobRecoveryRecord() {
        super(JobRecovery.JOB_RECOVERY);
    }

    /**
     * Create a detached, initialised JobRecoveryRecord
     */
    public JobRecoveryRecord(Integer id, String tenantId, String conditionCode, String testerType, String testerParms, String policyType, String policyParms, Integer numAttempts, LocalDateTime nextAttempt, LocalDateTime created, LocalDateTime lastUpdated, String testerHash) {
        super(JobRecovery.JOB_RECOVERY);

        set(0, id);
        set(1, tenantId);
        set(2, conditionCode);
        set(3, testerType);
        set(4, testerParms);
        set(5, policyType);
        set(6, policyParms);
        set(7, numAttempts);
        set(8, nextAttempt);
        set(9, created);
        set(10, lastUpdated);
        set(11, testerHash);
    }
}
