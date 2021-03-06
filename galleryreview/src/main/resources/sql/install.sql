create table image_file (id bigint generated by default as identity (start with 1), deleted boolean, fk_location bigint, to_delete boolean, name varchar(255), path varchar(300), reviewed boolean, version integer, primary key (id));
create table location (id bigint generated by default as identity (start with 1), name varchar(200), path varchar(300), delflag varchar(50), version integer, fk_review bigint, primary key (id));
create table review (id bigint generated by default as identity (start with 1), name varchar(100), startDate timestamp, version integer, primary key (id));
alter table image_file add constraint FK_48btg1xgyelf4yol6lb0w7c2a foreign key (fk_location) references location;
alter table location add constraint FK_7kxyd88ww7j07wp2whp941sey foreign key (fk_review) references review;
