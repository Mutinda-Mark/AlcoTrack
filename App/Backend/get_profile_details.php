<?php
header('Content-Type: application/json');

$servername = "192.168.158.182";
$username = "root"; 
$password = ""; 
$dbname = "project-1"; 
// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode(["error" => "Connection failed: " . $conn->connect_error]));
}

$email = $_GET['email'];

// Prepare SQL statement to prevent SQL injection
$stmt = $conn->prepare("SELECT D_FName, D_LName, D_No, D_Pass FROM tbl_driver WHERE D_ID = ?");
$stmt->bind_param("s", $email); // Assuming D_ID is a string.

$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode(["data" => [
        "FirstName" => $row['D_FName'],
        "LastName" => $row['D_LName'],
        "Phone" => $row['D_No'],
        "Password" => $row['D_Pass']
    ]]);
} else {
    echo json_encode(["message" => "No user found"]);
}

$stmt->close();
$conn->close();
?>
