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
    die(json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error)));
}

// Get POST data
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if (!empty($email) && !empty($password)) {
    // Query to check if user exists
    $stmt = $conn->prepare("SELECT D_FName, D_LName, D_Pass FROM tbl_driver WHERE D_ID = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        // User exists, fetch details
        $user = $result->fetch_assoc();

        // Verify password
        if ($password == $user['D_Pass']) {
            // Login successful
            echo json_encode(array("status" => "success", "message" => "Login successful", "FName" => $user['D_FName'], "LName" => $user['D_LName']));
        } else {
            // Invalid password
            echo json_encode(array("status" => "error", "message" => "Invalid password"));
        }
    } else {
        // User does not exist
        echo json_encode(array("status" => "error", "message" => "User not found"));
    }

    $stmt->close();
} else {
    echo json_encode(array("status" => "error", "message" => "Email or password not provided"));
}

$conn->close();
?>
