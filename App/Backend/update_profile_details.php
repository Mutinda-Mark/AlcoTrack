<?php
header('Content-Type: application/json');

// Decode the incoming JSON data
$data = json_decode(file_get_contents("php://input"), true);

// Check if JSON decoding was successful
if (json_last_error() !== JSON_ERROR_NONE) {
    echo json_encode(["error" => "JSON decode error: " . json_last_error_msg()]);
    exit();
}

// Database connection parameters
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

// Extract data from the JSON object
$firstName = $data['FirstName'] ?? null;
$lastName = $data['LastName'] ?? null;
$email = $data['Email'] ?? null;
$phone = $data['Phone'] ?? null;
$pass = $data['Password'] ?? null; // Assuming you're updating the password too

// Check for required fields
if (!$firstName || !$lastName || !$email || !$phone || !$pass) {
    echo json_encode(["error" => "Missing required fields."]);
    exit();
}

// Prepare SQL statement to prevent SQL injection
$stmt = $conn->prepare("UPDATE tbl_driver SET D_FName = ?, D_LName = ?, D_No = ?, D_Pass = ? WHERE D_ID = ?");
$stmt->bind_param("ssiss", $firstName, $lastName, $phone, $pass, $email); // Ensure the types match

// Execute the statement
if ($stmt->execute()) {
    echo json_encode(["message" => "Profile updated successfully"]);
} else {
    echo json_encode(["message" => "Error updating profile: " . $stmt->error]);
}

// Close statement and connection
$stmt->close();
$conn->close();
?>
