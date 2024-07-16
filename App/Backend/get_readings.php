<?php
header('Content-Type: application/json');

// Database connection settings
$servername = "192.168.158.182";
$username = "root"; 
$password = ""; 
$dbname = "project-1"; 

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error)));
}

// Query to get the latest alcohol reading from tbl_values
$sql = "SELECT V_AlcoholLevel FROM tbl_values ORDER BY V_Timestamp DESC LIMIT 1";
$result = $conn->query($sql);

if ($result) {
    if ($result->num_rows > 0) {
        // Reading exists, fetch details
        $reading = $result->fetch_assoc();
        $alcoholLevel = $reading['V_AlcoholLevel'];

        echo json_encode(array("status" => "success", "V_AlcoholLevel" => $alcoholLevel));
    } else {
        // No readings found
        echo json_encode(array("status" => "error", "message" => "No readings found"));
    }
} else {
    // Error executing query
    echo json_encode(array("status" => "error", "message" => "Error: " . $conn->error));
}

$conn->close();
?>
