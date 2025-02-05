import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import { Stack } from "@mui/material";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import { User } from "../types/types.ts";
import { login, register } from "../services/api/authApi.ts";

export default function RegisterPage() {
    const [user, setUser] = useState<User>({ username: "", password: "" });
    const [confirmPass, setConfirmPass] = useState<string>("");
    const [error, setError] = useState<boolean>(false);
    const navigate = useNavigate();

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setError(false);
        setUser({ ...user, [event.target.name]: event.target.value });
    };

    const handleConfirmChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setConfirmPass(event.target.value);
        setError(false);
    };

    const handleRegister = () => {
        if (confirmPass != user.password) {
            setError(true);
            return;
        }

        register(user)
            .then(() => login(user))
            .then(() => navigate("/"))
            .catch(() => setError(true));
    };

    return (
        <Box sx={{ ml: 0, mr: 0, textAlign: "center" }}>
            <Box sx={{ mt: 5 }}>
                <img src={"/icons/android-chrome-512x512.png"} alt={"LOGO"} width={"100px"} />
            </Box>
            <Typography variant={"h3"} sx={{ m: 5, mt: 1 }}>
                PairCode
            </Typography>

            <Box className="login-form">
                <Stack spacing={2} alignItems="center" mt={2}>
                    <Typography variant="h5">Register</Typography>
                    <TextField name="username" label="Username" onChange={handleChange} />
                    <TextField
                        type="password"
                        name="password"
                        label="Password"
                        error={error}
                        onChange={handleChange}
                    />
                    <TextField
                        type="password"
                        name="confirmPassword"
                        label="Confirm Password"
                        error={error}
                        onChange={handleConfirmChange}
                    />
                    <Button sx={{ minWidth: 245 }} variant="contained" color="primary" onClick={handleRegister}>
                        Register
                    </Button>
                    <Typography>
                        Already have an account?
                        <Button
                            variant={"text"}
                            color={"primary"}
                            onClick={() => navigate("/login")}
                            sx={{ textTransform: "none" }}
                        >
                            Log in
                        </Button>
                    </Typography>
                </Stack>
            </Box>
        </Box>
    );
}
