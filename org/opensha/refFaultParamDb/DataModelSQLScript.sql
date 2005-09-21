drop table Event_Sequence_Event_List;
drop table Event_Sequence_References;
drop table Event_Sequence;
drop table Paleo_Event_References;
drop table Paleo_Event;
drop table Combined_Events_References;
drop table Combined_Events_Info;
drop table Paleo_Site_References;
drop trigger Paleo_Site_Trigger;
drop sequence Paleo_Site_Sequence;
drop table Paleo_Site;
drop trigger Site_type_Trigger;
drop sequence Site_type_Sequence;
drop table site_type;
drop table Fault_Model_Section;
drop trigger Fault_Model_Trigger;
drop sequence Fault_Model_Sequence;
drop table Fault_Model;
drop table Fault_Section;
drop trigger Section_Source_Trigger;
drop sequence Section_Source_Sequence;
drop table Section_Source;
drop table PDF_Est;
drop table Log_Normal_Est;
drop table Log_type;
drop table XY_Est;
drop table Normal_Est;
drop trigger Est_Instances_Trigger;
drop sequence Est_Instances_Sequence;
drop table Est_Instances;
drop table Est_Type;
drop trigger Contributors_Trigger;
drop sequence Contributors_Sequence;
drop table Contributors;
drop trigger Reference_Trigger;
drop sequence Reference_Sequence;
drop table Reference;
drop table Site_Representations;

CREATE TABLE Site_Representations(
  Site_Representation_Id INTEGER NOT NULL,
  Site_Representation_Name VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY (Site_Representation_Id)
);





CREATE TABLE Reference (
  Reference_Id INTEGER NOT NULL,
  Short_Citation VARCHAR(255) NOT NULL UNIQUE,
  Full_Bibliographic_Reference VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY(Reference_Id)
);

create sequence Reference_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Reference_Trigger
before insert on Reference 
for each row
begin
if :new.Reference_Id  is null then
select  Reference_Sequence.nextval into :new.Reference_Id  from dual;
end if;
end;
/



CREATE TABLE Contributors (
  Contributor_Id INTEGER NOT NULL,
  Contributor_Name VARCHAR(45) NOT NULL UNIQUE,
  PRIMARY KEY(Contributor_Id)
);


create sequence Contributors_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Contributors_Trigger
before insert on Contributors 
for each row
begin
if :new.Contributor_Id  is null then
select  Contributors_Sequence.nextval into :new.Contributor_Id  from dual;
end if;
end;
/



CREATE TABLE Est_Type (
  Est_Type_Id INTEGER NOT NULL,
  Est_Name VARCHAR(45) NOT NULL UNIQUE,
  Entry_Date date NOT NULL,
  PRIMARY KEY(Est_Type_Id)
);




CREATE TABLE Est_Instances (
  Est_Id INTEGER NOT NULL,
  Est_Type_Id INTEGER  NOT NULL,
  Est_Units VARCHAR(45) NULL,
  Comments VARCHAR(255) NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Type_Id) 
    REFERENCES Est_Type(Est_Type_Id)
);

create sequence Est_Instances_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Est_Instances_Trigger
before insert on Est_Instances 
for each row
begin
if :new.Est_Id is null then
select Est_Instances_Sequence.nextval into :new.Est_Id from dual;
end if;
end;
/


CREATE TABLE Normal_Est (
  Est_Id INTEGER  NOT NULL,
  Mean FLOAT NOT NULL,
  Std_Dev FLOAT NOT NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Instances(Est_Id)
);

CREATE TABLE XY_Est (
  X FLOAT NOT NULL,
  Est_Id INTEGER NOT NULL,
  Y FLOAT NULL,
  PRIMARY KEY(X, Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Instances(Est_Id)
);

CREATE TABLE Log_Type (
  Log_Type_Id INTEGER NOT NULL,
  Log_Base VARCHAR(20) NOT NULL UNIQUE,
  PRIMARY KEY(Log_Type_Id)
);



CREATE TABLE Log_Normal_Est (
  Est_Id INTEGER NOT NULL,
  Log_Type_Id INTEGER NOT NULL,
  Median FLOAT NOT NULL,
  Std_Dev FLOAT NOT NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Log_Type_Id)
     REFERENCES Log_Type(Log_Type_Id)
);

CREATE TABLE PDF_Est (
  Est_Id INTEGER  NOT NULL,
  Min_X FLOAT NOT NULL,
  Delta_X FLOAT NOT NULL,
  Num INTEGER  NOT NULL,
  PRIMARY KEY(Est_Id),
  FOREIGN KEY(Est_Id)
    REFERENCES Est_Instances(Est_Id)
);



CREATE TABLE Section_Source (
  Section_Source_Id INTEGER NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Section_Source_Name VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY(Section_Source_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id) 
);


create sequence Section_Source_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Section_Source_Trigger
before insert on Section_Source 
for each row
begin
if :new.Section_Source_Id  is null then
select  Section_Source_Sequence.nextval into :new.Section_Source_Id  from dual;
end if;
end;
/



CREATE TABLE Fault_Section (
  Section_Id INTEGER  NOT NULL,
  Fault_Id INTEGER  NOT NULL,
  Section_Source_Id INTEGER  NOT NULL,
  Ave_Long_Term_Slip_Rate_Est INTEGER  NOT NULL,
  Ave_Dip_Est INTEGER  NOT NULL,
  Ave_Rake_Est INTEGER  NOT NULL,
  Ave_Upper_Depth_Est INTEGER  NOT NULL,
  Ave_Lower_Depth_Est INTEGER  NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Name VARCHAR(255) NOT NULL,
  Entry_Date date NOT NULL,
  Comments VARCHAR(255) NULL,
  Fault_Trace VARCHAR(255) NOT NULL,
  Aseismic_Slip_Factor_Est INTEGER  NOT NULL,
  PRIMARY KEY(Section_Id, Fault_Id, Section_Source_Id, Entry_Date),
  FOREIGN KEY(Section_Source_Id)
    REFERENCES Section_Source(Section_Source_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Ave_Long_Term_Slip_Rate_Est)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Ave_Dip_Est)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Ave_Rake_Est)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Ave_Upper_Depth_Est)
     REFERENCES Est_Instances(Est_Id), 
  FOREIGN KEY(Ave_Lower_Depth_Est)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Aseismic_Slip_Factor_Est)
     REFERENCES Est_Instances(Est_Id)
);


CREATE TABLE Fault_Model (
  Fault_Model_Id INTEGER NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Fault_Model_Name VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY(Fault_Model_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id) 
);


create sequence Fault_Model_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Fault_Model_Trigger
before insert on Fault_Model 
for each row
begin
if :new.Fault_Model_Id  is null then
select  Fault_Model_Sequence.nextval into :new.Fault_Model_Id  from dual;
end if;
end;
/

CREATE TABLE Fault_Model_Section (
  Fault_Model_Id INTEGER NOT NULL,
  Section_Id INTEGER  NOT NULL,
  Fault_Id INTEGER  NOT NULL,
  Section_Source_Id INTEGER  NOT NULL,
  Section_Entry_Date date NOT NULL,
  PRIMARY KEY(Fault_Model_Id, Section_Id, Fault_Id, Section_Source_Id, Section_Entry_Date),
  FOREIGN KEY(Fault_Model_Id)
     REFERENCES  Fault_Model(Fault_Model_Id),
  FOREIGN KEY(Section_Id, Fault_Id, Section_Source_Id, Section_Entry_Date)
     REFERENCES Fault_Section(Section_Id, Fault_Id, Section_Source_Id, Entry_Date)
);


CREATE TABLE Site_Type (
  Site_Type_Id INTEGER NOT NULL ,
  Contributor_Id INTEGER NOT NULL,
  Site_Type VARCHAR(255) NOT NULL UNIQUE,
  General_Comments VARCHAR(255) NULL,
  PRIMARY KEY(Site_Type_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id)
);

create sequence Site_Type_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Site_Type_Trigger
before insert on Site_Type 
for each row
begin
if :new.Site_Type_Id  is null then
select  Site_Type_Sequence.nextval into :new.Site_Type_Id  from dual;
end if;
end;
/


CREATE TABLE Paleo_Site (
  Site_Id INTEGER NOT NULL,
  Fault_Id INTEGER NOT NULL,
  Entry_Date date NOT NULL,
  Entry_Comments VARCHAR(255) NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Site_Type_Id INTEGER NOT NULL,
  Site_Name VARCHAR(255) NOT NULL,
  Site_Lat1 FLOAT NOT NULL,
  Site_Lon1 FLOAT NOT NULL,
  Site_Elevation1 FLOAT NULL,
  Site_Lat2 FLOAT NULL,
  Site_Lon2 FLOAT NULL,
  Site_Elevation2 FLOAT NULL, 
  Representative_Strand_Index INTEGER NOT NULL,
  General_Comments VARCHAR(255) NULL,
  Old_Site_Id VARCHAR(20) NULL,
  PRIMARY KEY(Site_Id, Entry_Date, Contributor_Id),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Site_Type_Id)
     REFERENCES Site_Type(Site_Type_Id)
);


create sequence Paleo_Site_Sequence
start with 1
increment by 1
nomaxvalue;

create trigger Paleo_Site_Trigger
before insert on Paleo_Site 
for each row
begin
if :new.Site_Id  is null then
select  Paleo_Site_Sequence.nextval into :new.Site_Id  from dual;
end if;
end;
/


CREATE TABLE Paleo_Site_References (
  Site_Id INTEGER NOT NULL,
  Entry_Date date NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Reference_Id INTEGER  NOT NULL,
  PRIMARY KEY(Site_Id, Entry_Date, Contributor_Id, Reference_Id),
  FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id),
  FOREIGN KEY(Site_Id, Entry_Date, Contributor_Id) 
     REFERENCES Paleo_Site(Site_Id, Entry_Date, Contributor_Id)
);
  
  
  

CREATE TABLE Combined_Events_Info (
  Info_Id INTEGER  NOT NULL,
  Site_Id INTEGER  NOT NULL,
  Site_Contributor_Id INTEGER  NOT NULL,  
  Site_Entry_Date date NOT NULL,  
  Entry_Date date NOT NULL,
  Entry_Comments VARCHAR(255) NOT NULL,
  Contributor_Id INTEGER  NOT NULL,
  Start_Time_Est_Id INTEGER  NOT NULL,
  End_Time_Est_Id INTEGER  NOT NULL,
  Total_Slip_Est_Id INTEGER  NOT NULL,
  Slip_Rate_Est_Id INTEGER  NOT NULL,
  Num_Events_Est_Id INTEGER  NOT NULL,
  Aseismic_Slip_Factor_Est_Id INTEGER  NOT NULL,
  Info_Name VARCHAR(255) NOT NULL,
  General_Comments VARCHAR(255) NULL,
  Dated_Feature_Comments VARCHAR(255) NULL,
  PRIMARY KEY(Info_Id, Entry_Date, Contributor_Id),
  FOREIGN KEY (Site_Id, Site_Contributor_Id, Site_Entry_Date) 
    REFERENCES Paleo_Site(Site_Id, Contributor_Id, Entry_Date),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Start_Time_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(End_Time_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Total_Slip_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Slip_Rate_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Num_Events_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Aseismic_Slip_Factor_Est_Id)
     REFERENCES Est_Instances(Est_Id)
);


CREATE TABLE Combined_Events_References (
 combined_Events_Id INTEGER  NOT NULL,
 combined_Events_Contributor_Id INTEGER  NOT NULL,
 combined_Events_Entry_Date date NOT NULL,
 Reference_Id INTEGER  NOT NULL,
 PRIMARY KEY (combined_Events_Id, combined_Events_Contributor_Id, combined_Events_Entry_Date, Reference_Id),
 FOREIGN KEY (combined_Events_Id, combined_Events_Contributor_Id, combined_Events_Entry_Date)
   REFERENCES Combined_Events_Info(Info_Id, Contributor_Id, Entry_Date),
 FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id)
);
 
  	
 

CREATE TABLE Paleo_Event (
  Event_Id INTEGER NOT NULL ,
  Site_Id INTEGER  NOT NULL,
  Site_Contributor_Id INTEGER  NOT NULL,  
  Site_Entry_Date date NOT NULL,
  Contributor_Id INTEGER  NOT NULL,
  Event_Date_Est_Id INTEGER  NOT NULL,
  Displacement_Est_Id INTEGER NOT NULL,
  Event_Name VARCHAR(255) NOT NULL,
  Entry_Date date NOT NULL,
  Entry_Comments VARCHAR(255) NOT NULL,
  General_Comments VARCHAR(255) NULL,
  PRIMARY KEY(Event_Id, Contributor_Id, Entry_Date),
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Event_Date_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(Displacement_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY (Site_Id, Site_Contributor_Id, Site_Entry_Date) 
    REFERENCES Paleo_Site(Site_Id, Contributor_Id, Entry_Date)
);


CREATE TABLE Paleo_Event_References (
 Paleo_Event_Id INTEGER  NOT NULL,
 Paleo_Event_Contributor_Id INTEGER  NOT NULL,
 Paleo_Event_Entry_Date DATE NOT NULL,
 Reference_Id INTEGER  NOT NULL,
 PRIMARY KEY (Paleo_Event_Id, Paleo_Event_Contributor_Id, Paleo_Event_Entry_Date, Reference_Id),
 FOREIGN KEY (Paleo_Event_Id, Paleo_Event_Contributor_Id, Paleo_Event_Entry_Date)
   REFERENCES Paleo_Event(Event_Id, Contributor_Id, Entry_Date),
 FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id)
);




CREATE TABLE Event_Sequence (
  Sequence_Id INTEGER NOT NULL,
  Site_Id INTEGER  NOT NULL,
  Site_Contributor_Id INTEGER  NOT NULL,  
  Site_Entry_Date date NOT NULL,
  Contributor_Id INTEGER NOT NULL,
  Start_Time_Est_Id INTEGER NOT NULL,
  End_Time_Est_Id INTEGER  NOT NULL,
  Sequence_Name VARCHAR(255) NOT NULL,
  Entry_Date date NOT NULL,
  Entry_Comments VARCHAR(255) NOT NULL, 
  General_Comments VARCHAR(255) NOT NULL,
  PRIMARY KEY(Sequence_Id, Contributor_Id, Entry_Date),  
  FOREIGN KEY(Contributor_Id)
     REFERENCES Contributors(Contributor_Id),
  FOREIGN KEY(Start_Time_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY(End_Time_Est_Id)
     REFERENCES Est_Instances(Est_Id),
  FOREIGN KEY (Site_Id, Site_Contributor_Id, Site_Entry_Date) 
    REFERENCES Paleo_Site(Site_Id, Contributor_Id, Entry_Date)
);

CREATE TABLE Event_Sequence_References (
  Event_Sequence_Id INTEGER  NOT NULL,
  Event_Sequence_Contributor_Id INTEGER  NOT NULL,
  Event_Sequence_Entry_Date DATE NOT NULL,
  Reference_Id INTEGER  NOT NULL,
  PRIMARY KEY (Event_Sequence_Id, Event_Sequence_Contributor_Id, Event_Sequence_Entry_Date, Reference_Id),
  FOREIGN KEY (Event_Sequence_Id, Event_Sequence_Contributor_Id, Event_Sequence_Entry_Date)
    REFERENCES Event_Sequence(Sequence_Id, Contributor_Id, Entry_Date),
  FOREIGN KEY(Reference_Id)
     REFERENCES Reference(Reference_Id)
); 


CREATE TABLE Event_Sequence_Event_List (
  Event_Id INTEGER  NOT NULL,
  Event_Contributor_Id  INTEGER  NOT NULL, 
  Event_Entry_Date DATE NOT NULL,
  Sequence_Id INTEGER  NOT NULL,
  Sequence_Contributor_Id  INTEGER  NOT NULL, 
  Sequence_Entry_Date date NOT NULL,
  Missed_Prob FLOAT NOT NULL,
  Event_Index_In_Sequence INTEGER  NOT NULL,
  PRIMARY KEY(Event_Id, Event_Contributor_Id, Event_Entry_Date, Sequence_Id, Sequence_Contributor_Id, Sequence_Entry_Date),
  FOREIGN KEY(Event_Id, Event_Contributor_Id, Event_Entry_Date)
   REFERENCES Paleo_Event(Event_Id, Contributor_Id, Entry_Date),
  FOREIGN KEY(Sequence_Id, Sequence_Contributor_Id, Sequence_Entry_Date)
   REFERENCES Event_Sequence(Sequence_Id, Contributor_Id, Entry_Date)  
);



insert into Reference (Short_Citation, Full_Bibliographic_Reference) values ('Short Citation 1','Full Bibliographic Reference 1');
insert into Reference (Short_Citation, Full_Bibliographic_Reference) values ('Short Citation 2','Full Bibliographic Reference 2');
insert into Reference (Short_Citation, Full_Bibliographic_Reference) values ('Short Citation 3','Full Bibliographic Reference 3');
insert into Reference (Short_Citation, Full_Bibliographic_Reference) values ('Short Citation 4','Full Bibliographic Reference 4');


insert into Site_Representations(Site_Representation_Id,Site_Representation_Name) values (1, 'Entire Fault');
insert into Site_Representations(Site_Representation_Id,Site_Representation_Name) values (2, 'Most Significant Strand');
insert into Site_Representations(Site_Representation_Id,Site_Representation_Name) values (3, 'One of Several Strands');
insert into Site_Representations(Site_Representation_Id,Site_Representation_Name) values (4, 'Unknown');

insert into Contributors(Contributor_Id, Contributor_name) values (1, 'fault_sandbox');

insert into Est_Type values(1,'NormalEstimate',sysdate);
insert into Est_Type values(2,'LogNormalEstimate',sysdate);
insert into Est_Type values(3,'PDF_Estimate',sysdate);
insert into Est_Type values(4,'FractileListEstimate',sysdate);
insert into Est_Type values(5,'IntegerEstimate',sysdate);
insert into Est_Type values(6,'DiscreteValueEstimate',sysdate);

insert into Log_Type values(1, '10');
insert into Log_Type values(2, 'E');

insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Between Locations', 'Between Locations Site Type');
insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Trench', 'Trench Site Type');
insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Geologic', 'Geologic Site Type');
insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Survey/Cultural', 'Survey/Cultural Site Type');
insert into Site_Type(Contributor_Id, Site_Type, General_Comments) values (1, 'Unknown', 'Unknown Site Type');

commit;


