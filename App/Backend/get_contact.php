<?php
$servername = "192.168.158.182";
$username = "root"; 
$password = ""; 
$dbname = "project-1";  // Update with your database name

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'];

    // Prepare SQL query
    $stmt = $conn->prepare("SELECT C_ID, C_FName, C_LName, C_No, D_ID FROM tbl_contact WHERE D_ID = ?");
    $stmt->bind_param("s", $email);

    // Execute query and fetch results
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        $contact = $result->fetch_assoc();
        echo json_encode([
            "status" => "success",
            "contact" => $contact
        ]);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "No contact found"
        ]);
    }

    $stmt->close();
}

$conn->close();
?>
