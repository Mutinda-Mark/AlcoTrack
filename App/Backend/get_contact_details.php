<?php

// Database configuration
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

// Fetch email parameter from GET request
if(isset($_GET['email'])) {
    $email = $_GET['email'];

    // Prepare SQL statement to fetch contact details
    $stmt = $conn->prepare("SELECT C_FName, C_LName, C_No, C_ID FROM tbl_contact WHERE D_ID = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();

    // Bind result variables
    $stmt->bind_result($C_FName, $C_LName, $C_No, $C_ID);

    // Fetch values
    if ($stmt->fetch()) {
        // Prepare JSON response
        $response = array(
            "C_FName" => $C_FName,
            "C_LName" => $C_LName,
            "C_No" => $C_No,
            "C_ID" => $C_ID
        );
        echo json_encode($response);
    } else {
        // No records found
        echo json_encode(array("message" => "No contact details found for the provided email"));
    }

    // Close statement
    $stmt->close();
} else {
    // Email parameter not provided
    echo json_encode(array("message" => "Email parameter is required"));
}

// Close connection
$conn->close();

?>
