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

// Get the JSON data from the request
$data = json_decode(file_get_contents('php://input'), true);

$timestamp = $data['V_Timestamp'];
$longitude = $data['V_Longitude'];
$latitude = $data['V_Latitude'];
$alcoholLevel = $data['V_AlcoholLevel'];

// Prepare and bind
$stmt = $conn->prepare("INSERT INTO tbl_values (V_Timestamp, V_Longitude, V_Latitude, V_AlcoholLevel) VALUES (?, ?, ?, ?)");
$stmt->bind_param("sddi", $timestamp, $longitude, $latitude, $alcoholLevel);

// Execute and check for success
if ($stmt->execute()) {
    echo "New record created successfully";
} else {
    echo "Error: " . $stmt->error;
}

$stmt->close();
$conn->close();
?>
