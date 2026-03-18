-- Fix: drop stale leave_date column from doctor_leaves table
-- This column was left over from before the upgrade to date-range leaves (leaveStartDate / leaveEndDate)
-- Run this once against medicare_v2_db

USE medicare_v2_db;

-- Drop the old single-date column if it still exists
ALTER TABLE doctor_leaves DROP COLUMN IF EXISTS leave_date;
