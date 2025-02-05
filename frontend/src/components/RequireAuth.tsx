import { ReactNode, useEffect } from "react";
import { useNavigate } from "react-router-dom";

type RequireAuthProps = {
    children: ReactNode;
};

/** Redirects unauthenticated users to the login page. */
export default function RequireAuth({ children }: RequireAuthProps) {
    const navigate = useNavigate();

    useEffect(() => {
        const token = sessionStorage.getItem("jwt");
        if (!token) {
            navigate("/login");
        }
    }, [navigate]);

    return <>{children}</>;
}
