<?php
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

// Get POST data for driver
$firstName = $_POST['firstName'];
$lastName = $_POST['lastName'];
$phone = $_POST['phone'];
$email = $_POST['email'];
$password = $_POST['password'];

// Get POST data for contact person
$contactFirstName = $_POST['contactFirstName'];
$contactLastName = $_POST['contactLastName'];
$contactPhone = $_POST['contactPhone'];
$contactEmail = $_POST['contactEmail'];

// Prepare and bind for driver table
$stmtDriver = $conn->prepare("INSERT INTO tbl_driver (D_FName, D_LName, D_No, D_ID, D_Pass) VALUES (?, ?, ?, ?, ?)");
$stmtDriver->bind_param("ssiss", $firstName, $lastName, $phone, $email, $password);

// Prepare and bind for contact person table
$stmtContact = $conn->prepare("INSERT INTO tbl_contact (C_FName, C_LName, C_No, C_ID, D_ID) VALUES (?, ?, ?, ?, ?)");
$stmtContact->bind_param("ssiss", $contactFirstName, $contactLastName, $contactPhone, $contactEmail, $email);

// Execute queries
$driverSuccess = $stmtDriver->execute();
$contactSuccess = $stmtContact->execute();

if ($driverSuccess && $contactSuccess) {
    echo json_encode(array("status" => "success", "message" => "Registration successful"));
} else {
    echo json_encode(array("status" => "error", "message" => "Registration failed"));
}

$stmtDriver->close();
$stmtContact->close();
$conn->close();
?>
