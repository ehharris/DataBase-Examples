-- Print Contents of BorrowedBy Table --
SELECT * FROM BorrowedBy;

-- For each member that has a book checked out (Last name, first name, member id), print a list of the book titles currently checked out --
SELECT Last_Name, First_Name, bb.MemberID, Title 
FROM BorrowedBy AS bb INNER JOIN Member AS m 
ON bb.MemberID=m.MemberID INNER JOIN Book b 
ON b.ISBN=bb.ISBN 
WHERE CheckinDate IS NULL;
