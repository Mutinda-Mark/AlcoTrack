<?php

// Database connection parameters
$servername = "192.168.158.182";
$username = "root"; 
$password = ""; 
$dbname = "project-1"; 

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get JSON input data
$data = json_decode(file_get_contents('php://input'), true);

// Extract data from JSON
$C_FName = $data['C_FName'];
$C_LName = $data['C_LName'];
$C_No = $data['C_No'];
$C_ID = $data['C_ID'];

// Update query
$sql = "UPDATE tbl_contact SET C_FName='$C_FName', C_LName='$C_LName', C_No='$C_No' WHERE C_ID='$C_ID'";

if ($conn->query($sql) === TRUE) {
    echo "Contact details updated successfully";
} else {
    echo "Error updating contact details: " . $conn->error;
}

$conn->close();

?>
