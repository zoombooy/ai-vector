@echo off
echo Creating Workflow tables...
mysql -u root -p123456 ai_manager < sql/create_workflow_tables.sql
echo Done!
pause

