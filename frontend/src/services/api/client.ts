import axios from "axios";

const apiClient = axios.create({
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
    const token = sessionStorage.getItem("jwt");
    if (token) {
        config.headers.Authorization = token.startsWith("Bearer ") ? token : `Bearer ${token}`;
    }
    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            sessionStorage.removeItem("jwt");
            sessionStorage.removeItem("username");
        }
        return Promise.reject(error);
    }
);

export default apiClient;

export const apiBaseUrl = () => import.meta.env.VITE_API_URL as string;
