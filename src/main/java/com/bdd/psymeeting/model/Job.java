/*
 * Copyright (c) 2020. Thomas GUILLAUME & Gabriel DUGNY
 */

package com.bdd.psymeeting.model;

import com.bdd.psymeeting.App;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class Job {

    // --------------------
    //   Attributes
    // --------------------

    private int jobId;
    private String JobName;
    private Date job_date;
    private int patientID;

    // --------------------
    //   Constructors
    // --------------------

    public Job(int jobId, String JobName, Date job_date) {
        this.jobId = jobId;
        this.JobName = JobName;
        this.job_date = job_date;
    }

    public Job(String JobName, Date job_date) {
        this.JobName = JobName;
        this.job_date = job_date;
    }

    // --------------------
    //   Get methods
    // --------------------


    /**
     * get all jobs like %jobName%
     */
    public static ArrayList<String> getJobLike(String jobName) {
        try (Connection connection = App.database.getConnection()) {
            if (!jobName.isEmpty()) {
                ArrayList<String> jobs = new ArrayList<>();
                String query = "select JOB_NAME from JOBS where JOB_NAME LIKE ? ";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, jobName.toUpperCase() + "%");
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next())
                    jobs.add(resultSet.getString(1));
                preparedStatement.close();
                return jobs;
            }
            return null;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Job> getJobByPatientID(int patientID) {
        try (Connection connection = App.database.getConnection()) {

            ArrayList<Job> listJob = new ArrayList<>();
            String query = "select JOBS.JOBS_ID, JOB_NAME, JOB_DATE from JOBS join PATIENTJOB P on JOBS.JOBS_ID = P.JOBS_ID where PATIENT_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, patientID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                listJob.add(new Job(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getDate(3)));
            }
            preparedStatement.close();
            return listJob;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // --------------------
    //   Set methods
    // --------------------

    public static int getLastPrimaryKeyID() {
        try (Connection connection = App.database.getConnection()) {
            Statement stmt = connection.createStatement();

            ResultSet resultSet = stmt.executeQuery("select max(JOBS_ID) from JOBS");
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -1;
            }

        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return -1;
    }

    /**
     * create the Job in DB with local state.
     *
     * @param jobs new jobs to be insert
     * @return true if succeeded
     */
    public static boolean insertJobs(ArrayList<Job> jobs) throws SQLException {

        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement1 = null;
        Connection connection = null;

        try {
            connection = App.database.getConnection();
            connection.setAutoCommit(false);

            String request1 = "INSERT INTO JOBS (JOBS_ID, JOB_NAME) VALUES (?,?)";
            String request2 = "INSERT INTO PATIENTJOB (JOBS_ID, PATIENT_iD, JOB_DATE) VALUES (?, ?, ?)";
            preparedStatement = connection.prepareStatement(request1);
            preparedStatement1 = connection.prepareStatement(request2);

            int lastID = Job.getLastPrimaryKeyID();
            if (lastID != -1) {

                // insert each job
                for (Job job : jobs
                ) {
                    if (!job.isExist()) { // if job name doesn't exist in database
                        // update new id
                        job.setJobId(lastID + 1);
                        preparedStatement.setInt(1, job.getJobId());
                        preparedStatement.setString(2, job.getJobName());
                        preparedStatement.executeUpdate();
                    } else {
                        int jobID = job.getJobDatabaseID(); // get the correct job ID
                        if (jobID != -1) {
                            job.setJobId(jobID);
                        } else {
                            return false; // error
                        }
                    }
                    // update patient-job
                    preparedStatement1.setInt(1, job.getJobId());
                    preparedStatement1.setInt(2, job.getPatientID());
                    preparedStatement1.setDate(3, job.getJobDate());
                    preparedStatement1.executeUpdate();

                    connection.commit();

                    lastID++;
                }
                return true;
            }

        } catch (SQLException ex) {
            System.err.println("Error to insert jobs in database");
            ex.printStackTrace();
            if (connection != null) {
                System.err.println("Transaction is being rolled back");
                connection.rollback();
            }
        } finally {
            if (preparedStatement != null) preparedStatement.close();
            if (preparedStatement1 != null) preparedStatement1.close();
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
        return false;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public Date getJobDate() {
        return job_date;
    }

    public void setJobDate(LocalDate jobDate) {
        this.job_date = new Date(jobDate.toEpochDay());
    }

    public String getJobName() {
        return JobName;
    }

    public void setJobName(String jobName) {
        this.JobName = jobName;
    }


    // --------------------
    //   Statement methods
    // --------------------

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public boolean isExist() throws SQLException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try (Connection connection = App.database.getConnection()) {

            String query = "select JOBS_ID from JOBS where JOB_NAME = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, this.getJobName());
            resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException ex) {
            System.err.println("Error to check if job exists");
            ex.printStackTrace();
            return false;
        } finally {
            if (preparedStatement != null) preparedStatement.close();
            if (resultSet != null) resultSet.close();
        }
    }

    public int getJobDatabaseID() throws SQLException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try (Connection connection = App.database.getConnection()) {

            String query = "select JOBS_ID from JOBS where JOB_NAME = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, this.getJobName());
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);

        } catch (SQLException ex) {
            System.err.println("Error to get job ID of " + this.getJobName());
            ex.printStackTrace();
            return 0;
        } finally {
            if (preparedStatement != null) preparedStatement.close();
            if (resultSet != null) resultSet.close();
        }
    }


}
